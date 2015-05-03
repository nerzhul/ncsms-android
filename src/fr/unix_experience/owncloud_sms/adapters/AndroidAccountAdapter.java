package fr.unix_experience.owncloud_sms.adapters;

import java.util.ArrayList;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AndroidAccountAdapter extends ArrayAdapter<Account> {

	private final ArrayList<Account> _accounts;
	private static int _itemLayout;
	private static int _accountFieldId;

	public AndroidAccountAdapter(final Context context, final int resource,
			final ArrayList<Account> objects, final int itemLayout, final int accountFieldId) {
		super(context, resource, objects);
		_accounts = objects;
		_itemLayout = itemLayout;
		_accountFieldId = accountFieldId;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(_itemLayout, null);
		}

		final Account account = _accounts.get(position);

		if (account != null) {
			final TextView label = (TextView) v.findViewById(_accountFieldId);
			if (label != null) {
				label.setText(account.name + "  -->");
			}
		}

		return v;
	}

}
