#include <jni.h>
#include <string>
#include <fstream>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_chu_opencv_NativeWrapper_stringFromJNI(
        JNIEnv *env,
        jobject obj) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT int JNICALL
Java_dev_chu_opencv_NativeWrapper_sum(
        JNIEnv *env,
        jobject obj,
        jint a,
        jint b) {
    return a + b;
}

extern "C"
JNIEXPORT void JNICALL
Java_dev_chu_opencv_NativeWrapper_convertRGBtoGray(
        JNIEnv *env,
        jobject thiz,
        jlong mat_addr_input,
        jlong mat_addr_result) {
    Mat &matInput = *(Mat *) mat_addr_input;
    Mat &matResult = *(Mat *) mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_chu_opencv_NativeWrapper_checkImage(
        JNIEnv *env,
        jobject obj,
        jstring filePath) {
    const char *path = (env)->GetStringUTFChars(filePath, 0);
    Mat image = cv::imread(path, IMREAD_COLOR);
    env->ReleaseStringUTFChars(filePath, path);

    if (!image.data) {
        return env->NewStringUTF("Image load failed!");
    } else {
        return env->NewStringUTF("Data is in image!");
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_dev_chu_opencv_NativeWrapper_loadImage(
        JNIEnv *env,
        jobject thiz,
        jstring _imageFileName,
        jlong img) {

    Mat &img_input = *(Mat *) img;
    const char *nativeFileNameString = env->GetStringUTFChars(_imageFileName, JNI_FALSE);

    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    img_input = imread(pathDir, IMREAD_COLOR);
}

extern "C"
JNIEXPORT void JNICALL
Java_dev_chu_opencv_NativeWrapper_imageProcessing(
        JNIEnv *env,
        jobject thiz,
        jlong inputImage,
        jlong outputImage) {

    Mat &img_input = *(Mat *) inputImage;
    Mat &img_output = *(Mat *) outputImage;

    cvtColor(img_input, img_input, COLOR_BGR2RGB);
    cvtColor(img_input, img_output, COLOR_RGB2GRAY);
    blur(img_output, img_output, Size(1, 1));
    Canny(img_output, img_output, 50, 150, 5);
}