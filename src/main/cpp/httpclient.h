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
#include "macro_helpers.h"

class HTTPClient
{
public:
    JNIEXPORT static jstring JNICALL getVersionCall(JNIEnv *env, jobject);
    JNIEXPORT static jstring JNICALL getAllSmsIdsCall(JNIEnv *env, jobject);
    JNIEXPORT static jstring JNICALL getLastMsgTimestamp(JNIEnv *env, jobject);
    JNIEXPORT static jstring JNICALL getPushRoute(JNIEnv *env, jobject);

    DECL_JNICLASSATTRS
};


