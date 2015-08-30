package fr.unix_experience.owncloud_sms.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactListAdapter extends ArrayAdapter<String> {
	private final ArrayList<String> _objects;
	private static int _itemLayout;
	private static int _fieldId;
	private Activity _activity;
	
	public ContactListAdapter(final Context context, final int resource,
				final ArrayList<String> objects, final int itemLayout,
				final int fieldId, final Activity activity) {
		super(context, resource, objects);
		_objects = objects;
		_itemLayout = itemLayout;
		_fieldId = fieldId;
		_activity = activity;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			final LayoutInflater inflater = 
					(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(_itemLayout, null);
		}

		if (_objects.size() == 0) {
			return null;
		}

		final String element = _objects.get(position);

		if (element != null) {
			final TextView label = (TextView) v.findViewById(_fieldId);
			if (label != null) {
				label.setText(element);
			}
		}

		return v;
	}

	private static final String TAG = ContactListAdapter.class.getSimpleName();
}
