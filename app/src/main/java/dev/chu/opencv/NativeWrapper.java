package dev.chu.opencv;

public class NativeWrapper {

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();
    public native int sum(int a, int b);
    public native void convertRGBtoGray(long matAddrInput, long matAddrResult);
}
