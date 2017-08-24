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
    SmsBuffer() = default;
    ~SmsBuffer() = default;

    JNIEXPORT static jlong JNICALL createNativeObject(JNIEnv *env, jobject self);
    JNIEXPORT static void JNICALL deleteNativeObject(JNIEnv *env, jobject self);

	/*
	 * push method
	 */
    JNIEXPORT static void JNICALL push(JNIEnv *env, jobject self, jint msg_id,
                                       jint mailbox_id, jint type, jlong date, jstring address,
                                       jstring body, jstring read, jstring seen);
    void _push(int msg_id, int mailbox_id, int type,
               long date, const char *address, const char *body, const char *read,
               const char *seen);

	/*
	 * empty method
	 */

	JNIEXPORT static jboolean JNICALL empty(JNIEnv *env, jobject self);
	bool _empty() const;

	/*
	 * print method
	 */
    JNIEXPORT static void JNICALL print(JNIEnv *env, jobject self);
    void _print();

	/*
	 * asRawJsonString method
	 */
	JNIEXPORT static jstring JNICALL asRawJsonString(JNIEnv *env, jobject self);
	void as_raw_json_string(std::string &result);

    DECL_JNICLASSATTRS

private:
    void reset_buffer();
    std::stringstream m_buffer;
	uint32_t m_sms_count{0};
    bool m_buffer_empty{true};

	static bool gJava_inited;
	static jfieldID gJava_mHandle;
};