/**
 *  Copyright (c) 2017, Loic Blot <loic.blot@unix-experience.fr>
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

#pragma once

#include <jni.h>
#include <sstream>
#include "macro_helpers.h"

class SmsBuffer
{
public:
    SmsBuffer();

    JNIEXPORT static jlong JNICALL createNativeObject(JNIEnv *env, jobject self);
    JNIEXPORT static void JNICALL deleteNativeObject(JNIEnv *env, jobject self, jlong ptr);

    JNIEXPORT static void JNICALL push(JNIEnv *env, jobject self, jlong ptr, jint mailbox_id);
    void _push(int mailbox_id);

    JNIEXPORT static void JNICALL print(JNIEnv *env, jobject self, jlong ptr);
    void _print();

    DECL_JNICLASSATTRS

private:
    void reset_buffer();
    std::stringstream m_buffer;
    bool m_buffer_empty{true};
};