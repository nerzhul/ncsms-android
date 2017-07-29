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

#include <android/log.h>
#include "smsbuffer.h"

#define LOG_TAG "SmsBuffer"
const char *SmsBuffer::classPathName = "fr/unix_experience/owncloud_sms/jni/SmsBuffer";

// See https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html
JNINativeMethod SmsBuffer::methods[] =
{
    DECL_JNIMETHOD(createNativeObject, "()J")
    DECL_JNIMETHOD(deleteNativeObject, "(J)V")
    DECL_JNIMETHOD(push, "(JI)V")
    DECL_JNIMETHOD(print, "(J)V")
};
DECL_METHODSIZE(SmsBuffer)

jlong SmsBuffer::createNativeObject(JNIEnv *env, jobject self)
{
    return reinterpret_cast<jlong>(new SmsBuffer());
}

void SmsBuffer::deleteNativeObject(JNIEnv *env, jobject self, jlong ptr)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "deleteSmsBuffer");
    delete reinterpret_cast<SmsBuffer *>(ptr);
}

SmsBuffer::SmsBuffer()
{
}

void SmsBuffer::reset_buffer()
{
    m_buffer.clear();
    m_buffer_empty = true;
}
void SmsBuffer::push(JNIEnv *env, jobject self, jlong ptr, jint mailbox_id)
{
    SmsBuffer *me = reinterpret_cast<SmsBuffer *>(ptr);
    if (!me) {
        __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "It's not a SmsBuffer!");
        return;
    }

    me->_push(mailbox_id);
}

void SmsBuffer::_push(int mailbox_id)
{
    // If buffer is not empty, we are joining messages
    if (!m_buffer_empty) {
        m_buffer << ",";
    }
    // Else, we are starting array
    else {
        m_buffer << "[";
        m_buffer_empty = true;
    }

    m_buffer << "{\"mbox\": " << mailbox_id << "}";
}

void SmsBuffer::print(JNIEnv *env, jobject self, jlong ptr)
{
    SmsBuffer *me = reinterpret_cast<SmsBuffer *>(ptr);
    if (!me) {
        __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "It's not a SmsBuffer!");
        return;
    }

    me->_print();
}

void SmsBuffer::_print()
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "SmsBuffer content: '%s'", m_buffer.str().c_str());
}