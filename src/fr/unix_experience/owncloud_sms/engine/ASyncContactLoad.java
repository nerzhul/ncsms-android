package fr.unix_experience.owncloud_sms.engine;

import android.os.AsyncTask;

public interface ASyncContactLoad {
	class ContactLoadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(final Void... params) {
			return null;

		}
	}

	static final String TAG = ASyncSMSSync.class.getSimpleName();
}
