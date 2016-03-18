package fr.unix_experience.owncloud_sms.adapters;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AndroidAccountAdapter extends ArrayAdapter<Account> {

	private final ArrayList<Account> _accounts;
	private static int _itemLayout;
	private static int _accountFieldId;
	private final Activity _activity;
	Class<?> _newActivityClass;

	public AndroidAccountAdapter(Activity activity, int resource,
			ArrayList<Account> objects, int itemLayout,
			int accountFieldId, Class<?> newActivityClass) {
		super(activity.getBaseContext(), resource, objects);
		_accounts = objects;
        AndroidAccountAdapter._itemLayout = itemLayout;
        AndroidAccountAdapter._accountFieldId = accountFieldId;
		_activity = activity;
		_newActivityClass = newActivityClass;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(AndroidAccountAdapter._itemLayout, null);
		}

		final Account account = _accounts.get(position);

		if (account != null) {
			TextView label = (TextView) v.findViewById(AndroidAccountAdapter._accountFieldId);
			if (label != null) {
				label.setText(account.name + " >");
				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
                    Intent i = new Intent(_activity, _newActivityClass);
					i.putExtra("account", account.name);
					_activity.startActivity(i);
					}
				});
			}
		}

		return v;
	}

}
