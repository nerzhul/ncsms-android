/**
 *  Copyright (c) 2014-2017, Loic Blot <loic.blot@unix-experience.fr>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <jni.h>
#include <android/log.h>
#include "httpclient.h"
#include "smsbuffer.h"

#define LOG_TAG "nativesms.cpp"

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
                                 JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;
    clazz = env->FindClass(className);
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Registering class '%s'", className);
    if (clazz == NULL) {
        __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "Native registration unable to find class %s", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "RegisterNatives failed for %s", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env,
                               HTTPClient::classPathName,
                               HTTPClient::methods,
                               HTTPClient::methods_size)) {
        return JNI_FALSE;
    }

    if (!registerNativeMethods(env,
                               SmsBuffer::classPathName,
                               SmsBuffer::methods,
                               SmsBuffer::methods_size)) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "NativeSMS library loading...");
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    registerNatives(env);

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "NativeSMS library loaded.");
    return JNI_VERSION_1_6;
}
