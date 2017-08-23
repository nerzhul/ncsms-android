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
#include <cassert>
#include <iomanip>
#include "smsbuffer.h"
#include "json.h"

#define LOG_TAG "SmsBuffer"
const char *SmsBuffer::classPathName = "fr/unix_experience/owncloud_sms/jni/SmsBuffer";

// See https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html
JNINativeMethod SmsBuffer::methods[] =
		{
				DECL_JNIMETHOD(createNativeObject, "()J")
				DECL_JNIMETHOD(deleteNativeObject, "(J)V")
				DECL_JNIMETHOD(empty, "(J)Z")
				DECL_JNIMETHOD(asRawJsonString, "(J)Ljava/lang/String;")
				DECL_JNIMETHOD(push,
							   "(JIIIJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")
				DECL_JNIMETHOD(print, "(J)V")
		};
DECL_METHODSIZE(SmsBuffer)

#define SMSBUFFER_CAST \
    SmsBuffer *me = reinterpret_cast<SmsBuffer *>(ptr); \
    if (!me) { \
        __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "It's not a SmsBuffer!"); \
		assert(false); \
    }

jlong SmsBuffer::createNativeObject(JNIEnv *env, jobject self)
{
	return reinterpret_cast<jlong>(new SmsBuffer());
}

void SmsBuffer::deleteNativeObject(JNIEnv *env, jobject self, jlong ptr)
{
	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "deleteNativeObject 0x%li", ptr);
	delete reinterpret_cast<SmsBuffer *>(ptr);
}

void SmsBuffer::reset_buffer()
{
	m_buffer.clear();
	m_buffer_empty = true;
	m_sms_count = 0;
}

void SmsBuffer::push(JNIEnv *env, jobject self, jlong ptr, jint msg_id, jint mailbox_id, jint type,
					 jlong date, jstring address, jstring body, jstring read, jstring seen)
{
	SMSBUFFER_CAST
	me->_push(msg_id, mailbox_id, type, date, env->GetStringUTFChars(address, NULL),
			  env->GetStringUTFChars(body, NULL), env->GetStringUTFChars(read, NULL),
			  env->GetStringUTFChars(seen, NULL));
}

void SmsBuffer::_push(int msg_id, int mailbox_id, int type,
					  long date, const char *address, const char *body, const char *read,
					  const char *seen)
{
	// If buffer is not empty, we are joining messages
	if (!m_buffer_empty) {
		m_buffer << ",";
	}
	// Else, we are starting array
	else {
		m_buffer << "[";
		m_buffer_empty = false;
	}

	m_buffer << "{\"_id\": " << msg_id << ", "
			<< "\"mbox\": " << mailbox_id << ", "
			<< "\"type\": " << type << ", "
			<< "\"date\": " << date << ", "
			<< "\"body\": " << json::escape_string(body) << ", "
			<< "\"address\": " << json::escape_string(address) << ", "
			<< "\"read\": " << json::escape_string(read) << ", "
			<< "\"seen\": " << json::escape_string(seen)
			<< "}";
	m_sms_count++;
}

jboolean SmsBuffer::empty(JNIEnv *env, jobject self, jlong ptr)
{
	SMSBUFFER_CAST
	return (jboolean) (me->_empty() ? 1 : 0);
}

bool SmsBuffer::_empty() const
{
	return m_buffer_empty;
}

void SmsBuffer::print(JNIEnv *env, jobject self, jlong ptr)
{
	SMSBUFFER_CAST
	me->_print();
}

void SmsBuffer::_print()
{
	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "SmsBuffer content: '%s'",
						m_buffer.str().c_str());
}

jstring SmsBuffer::asRawJsonString(JNIEnv *env, jobject self, jlong ptr)
{
	SMSBUFFER_CAST
	std::string result;
	me->as_raw_json_string(result);
	return env->NewStringUTF(result.c_str());
}

void SmsBuffer::as_raw_json_string(std::string &result)
{
	std::stringstream ss;
	ss << "{\"smsCount\": " << m_sms_count << ", \"smsDatas\": " << m_buffer.str() << "]}";
	result = ss.str();
}