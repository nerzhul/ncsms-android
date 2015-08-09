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

import java.util.List;
import java.util.Vector;

import org.json.JSONArray;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.activities.remote_account.AccountListActivity;
import fr.unix_experience.owncloud_sms.engine.ASyncSMSSync.SyncTask;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;
import fr.unix_experience.owncloud_sms.engine.SmsFetcher;
import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationManager;

public class MainActivity extends Activity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	PagerAdapter mPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.

		final List<Fragment> fragments = new Vector<Fragment>();

		/*
		 * Add the Main tabs here
		 */

		fragments.add(Fragment.instantiate(this,StarterFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,SecondTestFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,ThanksAndRateFragment.class.getName()));

		mPagerAdapter = new MainPagerAdapter(getFragmentManager(), fragments);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class MainPagerAdapter extends FragmentPagerAdapter {

		private final List<Fragment> mFragments;

		public MainPagerAdapter(final FragmentManager fragmentManager, final List<Fragment> fragments) {
			super(fragmentManager);
			mFragments = fragments;
		}

		@Override
		public Fragment getItem(final int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return mFragments.size();
		}
	}

	/**
	 * Fragments for activity must be there
	 */
	public static class StarterFragment extends Fragment {
		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_mainactivity_main, container,
					false);
			return rootView;
		}
	}

	public static class SecondTestFragment extends Fragment {
		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_mainactivity_gotosettings, container,
					false);
			return rootView;
		}
	}

	public static class ThanksAndRateFragment extends Fragment {
		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_mainactivity_thanks_note, container,
					false);
			return rootView;
		}
	}

	public void openAppSettings(final View view) {
		startActivity(new Intent(this, GeneralSettingsActivity.class));
	}

	public void openAddAccount(final View view) {
		startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
	}

	public void syncAllMessages(final View view) {
		final Context ctx = getApplicationContext();
		final ConnectivityMonitor cMon = new ConnectivityMonitor(ctx);

		if (cMon.isValid()) {
			// Now fetch messages since last stored date
			final JSONArray smsList = new SmsFetcher(ctx)
			.bufferizeMessagesSinceDate((long) 0);

			if (smsList != null) {
				final OCSMSNotificationManager nMgr = new OCSMSNotificationManager(ctx);
				nMgr.setSyncProcessMsg();
				new SyncTask(getApplicationContext(), smsList).execute();
			}
		}
		else {
			Toast.makeText(ctx, ctx.getString(R.string.err_sync_no_connection_available), Toast.LENGTH_SHORT).show();
		}
	}

	public void selectRemoteAccount(final View view) {
		startActivity(new Intent(this, AccountListActivity.class));
	}

	public void openGooglePlayStore(final View view) {
		Intent intent;
		try {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));

		} catch (final android.content.ActivityNotFoundException anfe) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
		}

		startActivity(intent);
	}
}
