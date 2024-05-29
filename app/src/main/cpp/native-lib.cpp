#include <jni.h>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_rn_sosnow_map_Keys_apiKey(JNIEnv *env, jobject thiz) {
    return env ->NewStringUTF("AIzaSyDQC7IjbyoaHX0mhDpLA6o2j-Q6FyB_U_w");  /* Here Api key*/
}