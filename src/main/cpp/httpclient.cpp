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

#include "httpclient.h"

const char *HTTPClient::classPathName = "fr/unix_experience/owncloud_sms/engine/OCHttpClient";

// See https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/types.html
JNINativeMethod HTTPClient::methods[] =
{
        DECL_JNIMETHOD(getAllSmsIdsCall, "()Ljava/lang/String;")
        DECL_JNIMETHOD(getLastMsgTimestamp, "()Ljava/lang/String;")
        DECL_JNIMETHOD(getPushRoute, "()Ljava/lang/String;")
        DECL_JNIMETHOD(getVersionCall, "()Ljava/lang/String;")
};
DECL_METHODSIZE(HTTPClient)

// APIv1 calls
#define RCALL_GET_VERSION "/index.php/apps/ocsms/get/apiversion?format=json"
#define RCALL_GET_ALL_SMS_IDS "/index.php/apps/ocsms/get/smsidlist?format=json"
#define RCALL_GET_LAST_MSG_TIMESTAMP "/index.php/apps/ocsms/get/lastmsgtime?format=json"
#define RCALL_PUSH_ROUTE "/index.php/apps/ocsms/push?format=json"

jstring HTTPClient::getVersionCall(JNIEnv *env, jobject)
{
    return env->NewStringUTF(RCALL_GET_VERSION);
}

jstring HTTPClient::getAllSmsIdsCall(JNIEnv *env, jobject)
{
    return env->NewStringUTF(RCALL_GET_ALL_SMS_IDS);
}

jstring HTTPClient::getLastMsgTimestamp(JNIEnv *env, jobject)
{
    return env->NewStringUTF(RCALL_GET_LAST_MSG_TIMESTAMP);
}

jstring HTTPClient::getPushRoute(JNIEnv *env, jobject)
{
    return env->NewStringUTF(RCALL_PUSH_ROUTE);
}
