package dev.chu.opencv;

public class NativeWrapper {

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();
    public native int sum(int a, int b);
    public native void convertRGBtoGray(long matAddrInput, long matAddrResult);
    public native String checkImage(String filePath);

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void loadImage(String imageFileName, long img);
    public native void imageProcessing(long inputImage, long outputImage);
}
