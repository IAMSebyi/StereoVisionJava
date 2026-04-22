# Stereo Vision 3D Reconstruction System - Stage I

This project implements **Stage I** for the Advanced Object-Oriented Programming lab. It uses **Java** and the **OpenCV Java bindings** to build a simplified stereo vision pipeline from a pair of stereo images.

## Short Description

The application computes a disparity map, a relative depth map, and a sparse relative 3D point cloud from two stereo images. It also keeps projects, stereo pairs, reconstruction sessions, results, and statistics in memory.

## Stage I Requirements Covered

### 1) At least 10 actions / queries

1. create a stereo project
2. list existing projects
3. list projects sorted by name
4. add a stereo image pair to a project
5. list stereo pairs for a project
6. set / update camera parameters
7. show available stereo algorithms
8. run reconstruction with StereoBM
9. run reconstruction with StereoSGBM
10. generate and export the disparity map
11. generate and export the relative depth map
12. generate and export relative 3D points to CSV
13. show reconstruction sessions for a project
14. show statistics for a reconstruction session

### 2) At least 8 object types

1. `StereoProject`
2. `StereoImagePair`
3. `CameraParameters`
4. `DisparityMapResult`
5. `DepthMapResult`
6. `Point3DData`
7. `ReconstructionStats`
8. `ReconstructionSession`
9. `AlgorithmConfig`
10. `StereoMatcherAlgorithm`

### 3) Simple classes with private / protected attributes and access methods

All model classes use `private` / `protected` attributes, constructors, getters, and setters.

### 4) At least 2 different collections, with at least one sorted collection

The project explicitly uses:

- `List<StereoProject>`
- `Map<Integer, StereoProject>`
- `SortedSet<StereoProject>` (`TreeSet`) - sorted collection
- `Set<Point3DData>` (`TreeSet`) - sorted collection for 3D points

### 5) Inheritance and derived classes stored in collections

- `StereoMatcherAlgorithm` is an abstract class
- `StereoBMAlgorithm` and `StereoSGBMAlgorithm` extend it
- derived objects are stored in `List<StereoMatcherAlgorithm>`

### 6) At least one service class

The project contains multiple services:

- `ProjectService`
- `ReconstructionService`
- `ExportService`

### 7) Main class

The entry point is `stereovision.Main`.

## Pipeline Notes

- input: two RGB / grayscale stereo images with the same size
- images are converted to grayscale
- disparity is computed with `StereoBM` or `StereoSGBM`
- the 16-bit OpenCV disparity output is converted to `float` and divided by 16
- if manual camera parameters are not provided, the application tries to estimate them from EXIF
- if EXIF data is missing or incomplete, it falls back to relative default parameters
- generated depth is **relative depth**, not guaranteed metric depth

## Automatic Camera Parameter Estimation

When the first stereo pair is added to a project, the application tries to estimate:

- `fx`, `fy` from EXIF focal length data
- `cx`, `cy` as the image center
- `baseline` as `1.0`, because normal EXIF data usually does not contain stereo baseline

Supported EXIF sources:

- `FocalLength` plus focal plane resolution tags, when available
- `FocalLengthIn35mmFilm`, as a fallback estimate

These values are still marked as approximate. For real metric 3D reconstruction, a stereo camera calibration is still required.

## Project Structure

```text
src/
  Main.java
  algorithm/
  config/
  model/
  service/
  util/
```

## Requirements

- JDK 17+ (JDK 11+ should also work)
- `opencv-4xx.jar`
- the OpenCV Java native library (`opencv_java4xx.dll` / `.so` / `.dylib`)

This project is currently configured in IntelliJ for:

```text
C:\opencv\build\java\opencv-4120.jar
C:\opencv\build\java\x64
```

Adjust the paths if your OpenCV installation is elsewhere.

## Compile

PowerShell example on Windows:

```powershell
javac -cp "C:\opencv\build\java\opencv-4120.jar" -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

Generic Unix-like example:

```bash
javac -cp "/path/to/opencv-4xx.jar" -d out $(find src -name "*.java")
```

## Run on Two Images

The application supports direct CLI execution:

```powershell
java "-Djava.library.path=C:\opencv\build\java\x64" -cp "out;C:\opencv\build\java\opencv-4120.jar" stereovision.Main "left.jpg" "right.jpg" "output" "StereoSGBM"
```

Arguments:

```text
stereovision.Main <left-image> <right-image> [output-dir] [StereoBM|StereoSGBM]
```

Defaults:

- `output-dir`: `output`
- algorithm: `StereoSGBM`

The run exports:

- disparity map: `*_disparity.png`
- disparity heatmap: `*_disparity_heatmap.png`
- invalid disparity mask: `*_invalid_disparity_mask.png`
- relative depth map: `*_depth.png`
- relative depth heatmap: `*_depth_heatmap.png`
- relative 3D points: `*_points.csv`

## Reading the Visual Outputs

The grayscale maps are normalized over valid pixels. The heatmaps use OpenCV's TURBO color map:

- dark blue / purple: lower value
- green / yellow: medium value
- orange / red: higher value
- black: invalid or missing value

For disparity, higher values usually mean the object is closer to the camera. For relative depth in this project, depth is computed as `1 / disparity`, so higher depth values usually mean farther points.

The invalid disparity mask marks pixels where the stereo matcher could not produce a useful positive disparity. Large red areas in this mask usually mean the pair is not rectified, the left/right images are mismatched, the scene has too little texture, or the disparity search range does not match the dataset.

## Input Image Guidelines

For `StereoBM` and `StereoSGBM`, the input pair should have these properties:

- both images must have exactly the same width and height
- the pair should be rectified, so corresponding points lie on the same image rows
- the cameras should be horizontally displaced, not rotated strongly between shots
- the left image must really be the left camera view and the right image the right camera view
- both images should have similar exposure, focus, color, and lens distortion
- the scene should contain enough texture; plain walls, sky, glass, reflections, and repeated patterns are difficult
- moving objects between the two captures should be avoided
- objects should not be so close that their disparity is larger than the configured search range
- dataset pairs should be used exactly as provided, without resizing only one side or mixing images from different frames

If the output looks mostly black or noisy, first try a known rectified stereo dataset pair and run `StereoSGBM`. `StereoBM` is faster but usually more fragile.

## Interactive Mode

Run without image arguments to use the console menu:

```powershell
java "-Djava.library.path=C:\opencv\build\java\x64" -cp "out;C:\opencv\build\java\opencv-4120.jar" stereovision.Main
```

Workflow:

1. Create a stereo project.
2. Add a left / right image pair.
3. Optionally configure `fx`, `fy`, `cx`, `cy`, and `baseline` manually.
4. Run reconstruction with `StereoBM` or `StereoSGBM`.
5. Inspect the exported disparity map, relative depth map, CSV points, and session statistics.

## Known Limitations for Stage I

- the project does not include JDBC and CSV audit yet; those are expected in Stage II
- it does not include full stereo calibration
- 3D reconstruction is relative, not guaranteed metric
- results are better for approximately rectified stereo images
