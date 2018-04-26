//
// Created by jonatanpoveda on 06/10/16.
//

#include <jni.h>
#include <string>
#include <math.h>

extern "C"
jstring Java_ie_ucc_jonatanpoveda_frontend_MainDevActivity_stringFromJNI(JNIEnv *env,
                                                                         jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jdoubleArray JNICALL Java_ie_ucc_jonatanpoveda_data_Complex64_CPolar2Rectangular(
        JNIEnv *env, jobject obj, double magnitude, double argument) {

    jdoubleArray result = env->NewDoubleArray(2);

    double *complex = new double[2];
    complex[0] = magnitude * cos(argument);
    complex[1] = magnitude * sin(argument);

    env->SetDoubleArrayRegion(result, 0, 2, complex);
    return result;
}

