package stereovision.util;

import stereovision.model.CameraParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ExifCameraEstimator {
    private static final int MARKER_APP1 = 0xE1;
    private static final int TAG_EXIF_IFD_POINTER = 0x8769;
    private static final int TAG_FOCAL_LENGTH = 0x920A;
    private static final int TAG_FOCAL_PLANE_X_RESOLUTION = 0xA20E;
    private static final int TAG_FOCAL_PLANE_Y_RESOLUTION = 0xA20F;
    private static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = 0xA210;
    private static final int TAG_FOCAL_LENGTH_35MM = 0xA405;
    private static final double DEFAULT_BASELINE = 1.0;

    private ExifCameraEstimator() {
    }

    public static CameraParameters estimateOrDefault(String leftImagePath,
                                                     String rightImagePath,
                                                     int imageWidth,
                                                     int imageHeight) {
        return estimate(leftImagePath, imageWidth, imageHeight)
                .or(() -> estimate(rightImagePath, imageWidth, imageHeight))
                .orElseGet(() -> CameraParameters.createRelativeDefault(imageWidth, imageHeight));
    }

    public static Optional<CameraParameters> estimate(String imagePath, int imageWidth, int imageHeight) {
        Optional<ExifData> exifData = readExifData(imagePath);
        if (exifData.isEmpty()) {
            return Optional.empty();
        }

        ExifData data = exifData.get();
        double cx = imageWidth / 2.0;
        double cy = imageHeight / 2.0;

        if (data.focalLengthMm > 0.0 && data.focalPlaneXResolution > 0.0) {
            double mmPerUnit = mmPerResolutionUnit(data.focalPlaneResolutionUnit);
            if (mmPerUnit > 0.0) {
                double fx = data.focalLengthMm * data.focalPlaneXResolution / mmPerUnit;
                double fy = data.focalPlaneYResolution > 0.0
                        ? data.focalLengthMm * data.focalPlaneYResolution / mmPerUnit
                        : fx;
                if (isUsableFocalLength(fx, imageWidth, imageHeight) && isUsableFocalLength(fy, imageWidth, imageHeight)) {
                    return Optional.of(new CameraParameters(0, fx, fy, cx, cy, DEFAULT_BASELINE, true));
                }
            }
        }

        if (data.focalLength35mm > 0.0) {
            double focalPixels = imageWidth * data.focalLength35mm / 36.0;
            if (isUsableFocalLength(focalPixels, imageWidth, imageHeight)) {
                return Optional.of(new CameraParameters(0, focalPixels, focalPixels, cx, cy, DEFAULT_BASELINE, true));
            }
        }

        return Optional.empty();
    }

    private static boolean isUsableFocalLength(double focalPixels, int imageWidth, int imageHeight) {
        int maxDimension = Math.max(imageWidth, imageHeight);
        return Double.isFinite(focalPixels) && focalPixels > 0.1 && focalPixels < maxDimension * 20.0;
    }

    private static double mmPerResolutionUnit(int unit) {
        return switch (unit) {
            case 2 -> 25.4;
            case 3 -> 10.0;
            case 4 -> 1.0;
            case 5 -> 0.001;
            default -> 0.0;
        };
    }

    private static Optional<ExifData> readExifData(String imagePath) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of(imagePath));
        } catch (IOException exception) {
            return Optional.empty();
        }

        int app1Start = findExifApp1Start(bytes);
        if (app1Start < 0 || app1Start + 14 >= bytes.length || !startsWithExifHeader(bytes, app1Start)) {
            return Optional.empty();
        }

        int tiffStart = app1Start + 6;
        boolean littleEndian;
        if (bytes[tiffStart] == 'I' && bytes[tiffStart + 1] == 'I') {
            littleEndian = true;
        } else if (bytes[tiffStart] == 'M' && bytes[tiffStart + 1] == 'M') {
            littleEndian = false;
        } else {
            return Optional.empty();
        }

        int magic = readUnsignedShort(bytes, tiffStart + 2, littleEndian);
        if (magic != 42) {
            return Optional.empty();
        }

        long ifd0Offset = readUnsignedInt(bytes, tiffStart + 4, littleEndian);
        int ifd0Start = checkedTiffOffset(tiffStart, ifd0Offset, bytes.length);
        if (ifd0Start < 0) {
            return Optional.empty();
        }

        Optional<Double> exifIfdOffset = readNumericTag(bytes, tiffStart, ifd0Start, littleEndian, TAG_EXIF_IFD_POINTER);
        if (exifIfdOffset.isEmpty()) {
            return Optional.empty();
        }

        int exifIfdStart = checkedTiffOffset(tiffStart, exifIfdOffset.get().longValue(), bytes.length);
        if (exifIfdStart < 0) {
            return Optional.empty();
        }

        ExifData exifData = new ExifData();
        exifData.focalLengthMm = readNumericTag(bytes, tiffStart, exifIfdStart, littleEndian, TAG_FOCAL_LENGTH)
                .orElse(0.0);
        exifData.focalLength35mm = readNumericTag(bytes, tiffStart, exifIfdStart, littleEndian, TAG_FOCAL_LENGTH_35MM)
                .orElse(0.0);
        exifData.focalPlaneXResolution = readNumericTag(bytes, tiffStart, exifIfdStart, littleEndian, TAG_FOCAL_PLANE_X_RESOLUTION)
                .orElse(0.0);
        exifData.focalPlaneYResolution = readNumericTag(bytes, tiffStart, exifIfdStart, littleEndian, TAG_FOCAL_PLANE_Y_RESOLUTION)
                .orElse(0.0);
        exifData.focalPlaneResolutionUnit = readNumericTag(bytes, tiffStart, exifIfdStart, littleEndian, TAG_FOCAL_PLANE_RESOLUTION_UNIT)
                .map(Double::intValue)
                .orElse(0);

        return Optional.of(exifData);
    }

    private static int findExifApp1Start(byte[] bytes) {
        if (bytes.length < 4 || (bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8) {
            return -1;
        }

        int position = 2;
        while (position + 4 < bytes.length) {
            if ((bytes[position] & 0xFF) != 0xFF) {
                return -1;
            }

            int marker = bytes[position + 1] & 0xFF;
            int segmentLength = readUnsignedShort(bytes, position + 2, false);
            if (segmentLength < 2 || position + 2 + segmentLength > bytes.length) {
                return -1;
            }

            if (marker == MARKER_APP1 && startsWithExifHeader(bytes, position + 4)) {
                return position + 4;
            }

            position += 2 + segmentLength;
        }

        return -1;
    }

    private static boolean startsWithExifHeader(byte[] bytes, int offset) {
        return offset + 6 <= bytes.length
                && bytes[offset] == 'E'
                && bytes[offset + 1] == 'x'
                && bytes[offset + 2] == 'i'
                && bytes[offset + 3] == 'f'
                && bytes[offset + 4] == 0
                && bytes[offset + 5] == 0;
    }

    private static Optional<Double> readNumericTag(byte[] bytes,
                                                   int tiffStart,
                                                   int ifdStart,
                                                   boolean littleEndian,
                                                   int tagToFind) {
        if (ifdStart + 2 >= bytes.length) {
            return Optional.empty();
        }

        int entryCount = readUnsignedShort(bytes, ifdStart, littleEndian);
        int entryStart = ifdStart + 2;
        for (int index = 0; index < entryCount; index++) {
            int entryOffset = entryStart + index * 12;
            if (entryOffset + 12 > bytes.length) {
                return Optional.empty();
            }

            int tag = readUnsignedShort(bytes, entryOffset, littleEndian);
            if (tag != tagToFind) {
                continue;
            }

            int type = readUnsignedShort(bytes, entryOffset + 2, littleEndian);
            long count = readUnsignedInt(bytes, entryOffset + 4, littleEndian);
            long valueOrOffset = readUnsignedInt(bytes, entryOffset + 8, littleEndian);
            return readFirstNumericValue(bytes, tiffStart, entryOffset + 8, valueOrOffset, type, count, littleEndian);
        }

        return Optional.empty();
    }

    private static Optional<Double> readFirstNumericValue(byte[] bytes,
                                                         int tiffStart,
                                                         int inlineValueOffset,
                                                         long valueOrOffset,
                                                         int type,
                                                         long count,
                                                         boolean littleEndian) {
        if (count < 1) {
            return Optional.empty();
        }

        int typeSize = typeSize(type);
        if (typeSize <= 0) {
            return Optional.empty();
        }

        long totalSize = typeSize * count;
        int valueOffset = totalSize <= 4
                ? inlineValueOffset
                : checkedTiffOffset(tiffStart, valueOrOffset, bytes.length);
        if (valueOffset < 0 || valueOffset + typeSize > bytes.length) {
            return Optional.empty();
        }

        return switch (type) {
            case 3 -> Optional.of((double) readUnsignedShort(bytes, valueOffset, littleEndian));
            case 4 -> Optional.of((double) readUnsignedInt(bytes, valueOffset, littleEndian));
            case 5 -> readUnsignedRational(bytes, valueOffset, littleEndian);
            case 9 -> Optional.of((double) readSignedInt(bytes, valueOffset, littleEndian));
            case 10 -> readSignedRational(bytes, valueOffset, littleEndian);
            default -> Optional.empty();
        };
    }

    private static Optional<Double> readUnsignedRational(byte[] bytes, int offset, boolean littleEndian) {
        if (offset + 8 > bytes.length) {
            return Optional.empty();
        }

        long numerator = readUnsignedInt(bytes, offset, littleEndian);
        long denominator = readUnsignedInt(bytes, offset + 4, littleEndian);
        if (denominator == 0) {
            return Optional.empty();
        }
        return Optional.of((double) numerator / denominator);
    }

    private static Optional<Double> readSignedRational(byte[] bytes, int offset, boolean littleEndian) {
        if (offset + 8 > bytes.length) {
            return Optional.empty();
        }

        int numerator = readSignedInt(bytes, offset, littleEndian);
        int denominator = readSignedInt(bytes, offset + 4, littleEndian);
        if (denominator == 0) {
            return Optional.empty();
        }
        return Optional.of((double) numerator / denominator);
    }

    private static int typeSize(int type) {
        return switch (type) {
            case 1, 2, 7 -> 1;
            case 3 -> 2;
            case 4, 9 -> 4;
            case 5, 10 -> 8;
            default -> -1;
        };
    }

    private static int checkedTiffOffset(int tiffStart, long offset, int length) {
        long absoluteOffset = tiffStart + offset;
        if (absoluteOffset < 0 || absoluteOffset >= length || absoluteOffset > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) absoluteOffset;
    }

    private static int readUnsignedShort(byte[] bytes, int offset, boolean littleEndian) {
        if (littleEndian) {
            return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
        }
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    private static long readUnsignedInt(byte[] bytes, int offset, boolean littleEndian) {
        if (littleEndian) {
            return ((long) bytes[offset] & 0xFF)
                    | (((long) bytes[offset + 1] & 0xFF) << 8)
                    | (((long) bytes[offset + 2] & 0xFF) << 16)
                    | (((long) bytes[offset + 3] & 0xFF) << 24);
        }
        return (((long) bytes[offset] & 0xFF) << 24)
                | (((long) bytes[offset + 1] & 0xFF) << 16)
                | (((long) bytes[offset + 2] & 0xFF) << 8)
                | ((long) bytes[offset + 3] & 0xFF);
    }

    private static int readSignedInt(byte[] bytes, int offset, boolean littleEndian) {
        return (int) readUnsignedInt(bytes, offset, littleEndian);
    }

    private static class ExifData {
        private double focalLengthMm;
        private double focalLength35mm;
        private double focalPlaneXResolution;
        private double focalPlaneYResolution;
        private int focalPlaneResolutionUnit;
    }
}
