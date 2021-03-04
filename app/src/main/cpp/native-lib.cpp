#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_dev_chu_opencv_NativeWrapper_stringFromJNI(
        JNIEnv *env,
        jobject obj) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT int JNICALL Java_dev_chu_opencv_NativeWrapper_sum
        (JNIEnv *env, jobject obj, jint a, jint b) {
    return a + b;
}

extern "C"
JNIEXPORT void JNICALL
Java_dev_chu_opencv_NativeWrapper_convertRGBtoGray(JNIEnv *env, jobject thiz, jlong mat_addr_input,
                                                   jlong mat_addr_result) {
    Mat &matInput = *(Mat *) mat_addr_input;
    Mat &matResult = *(Mat *) mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}