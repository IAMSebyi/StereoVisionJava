package stereovision.config;

import org.opencv.core.Core;

public final class OpenCVLoader {
    private static boolean loaded = false;

    private OpenCVLoader() {
    }

    public static void load() {
        if (!loaded) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            loaded = true;
        }
    }
}
