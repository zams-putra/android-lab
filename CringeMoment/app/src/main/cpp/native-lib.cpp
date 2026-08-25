#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL

// harus sesuai ini nama func nya kudu sinkron euy
Java_com_sebassmith_cringemoment_VaultNative_decodeSecret(JNIEnv *env, jobject /* this */) {
    unsigned char enc[] = {
            0x15,0x77,0x76,0x74,0x36,0x64,0x66,0x77,0x73,0x77,
            0x0
    };
    std::string out;
    for (unsigned char c : enc) {
        if (c == 0x0) break;
        out += (char)(c ^ 0x13);
    }
    return env->NewStringUTF(out.c_str());
}