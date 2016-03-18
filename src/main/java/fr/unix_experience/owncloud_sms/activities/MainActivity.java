package fr.unix_experience.owncloud_sms.activities;

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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.activities.remote_account.AccountListActivity;
import fr.unix_experience.owncloud_sms.engine.ASyncSMSSync.SyncTask;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;
import fr.unix_experience.owncloud_sms.engine.SmsFetcher;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationType;
import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationUI;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static ConnectivityMonitor mConnectivityMonitor = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        if (mConnectivityMonitor == null) {
            mConnectivityMonitor = new ConnectivityMonitor(getApplicationContext());
        }

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.openDrawer(GravityCompat.START);
	}

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean res = true;

        switch (id) {
            case R.id.nav_sync: res = syncAllMessages(); break;
            case R.id.nav_manage: res = openAppSettings(); break;
            case R.id.nav_rateus: res = openGooglePlayStore(); break;
            case R.id.nav_add_account: res = openAddAccount(); break;
            case R.id.nav_my_accounts: res = openMyAccounts(); break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return res;
    }

	private boolean openAppSettings () {
		startActivity(new Intent(this, OCSMSSettingsActivity.class));
        return true;
	}

    private boolean openAddAccount () {
		startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
        return true;
	}

    private boolean syncAllMessages () {
		Context ctx = getApplicationContext();

		if (mConnectivityMonitor.isValid()) {
			// Now fetch messages since last stored date
			JSONArray smsList = new JSONArray();
            new SmsFetcher(ctx).bufferMessagesSinceDate(smsList, (long) 0);

			if (smsList.length() > 0) {
                OCSMSNotificationUI.notify(ctx, ctx.getString(R.string.sync_title),
                        ctx.getString(R.string.sync_inprogress), OCSMSNotificationType.SYNC.ordinal());
				new SyncTask(getApplicationContext(), smsList).execute();
			}
		}
		else {
			Toast.makeText(ctx, ctx.getString(R.string.err_sync_no_connection_available), Toast.LENGTH_SHORT).show();
		}
        return true;
	}

    private boolean openMyAccounts () {
		startActivity(new Intent(this, AccountListActivity.class));
        return true;
	}

    private boolean openGooglePlayStore () {
		Intent intent;
		try {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
		} catch (android.content.ActivityNotFoundException anfe) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
		}

		startActivity(intent);
        return true;
	}
}
