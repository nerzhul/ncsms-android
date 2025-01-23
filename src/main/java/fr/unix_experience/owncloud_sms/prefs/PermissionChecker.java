package fr.unix_experience.owncloud_sms.prefs;

/*
 * Copyright (c) 2014-2015, Loic Blot <loic.blot@unix-experience.fr>
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationType;
import fr.unix_experience.owncloud_sms.enums.PermissionID;
import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationUI;

public class PermissionChecker {
    public static boolean checkPermission(Context context, final String permissionName,
                                          final PermissionID permissionId) {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(context, permissionName);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            // Check if we only have a context or a full activity
            final Activity activity = (context instanceof Activity) ? ((Activity) context) : null;
            if (activity != null) {
                // For activity notify directly the user
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionName)) {
                    PermissionChecker.showMessageOKCancel(activity, "You need to fix application permissions.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{ permissionName }, permissionId.ordinal());
                                }
                            });
                    return false;
                }

                ActivityCompat.requestPermissions(activity, new String[]{permissionName},
                        permissionId.ordinal());
                return false;
            }

            // For context only show a notification
            OCSMSNotificationUI.notify(context, context.getString(R.string.notif_permission_required),
                    context.getString(R.string.notif_permission_required_content),
                    OCSMSNotificationType.PERMISSION);

            return false;
        }

        return true;
    }

    private static void showMessageOKCancel(Activity activity, String message,
        DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.understood, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }
}
