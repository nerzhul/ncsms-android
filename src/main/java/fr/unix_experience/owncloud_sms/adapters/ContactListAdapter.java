package fr.unix_experience.owncloud_sms.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactListAdapter extends ArrayAdapter<String> {
	private final ArrayList<String> _objects;
	private static int _itemLayout;
	private static int _fieldId;
	private final Activity _activity;
	
	public ContactListAdapter(Context context, int resource,
				ArrayList<String> objects, int itemLayout,
				int fieldId, Activity activity) {
		super(context, resource, objects);
		_objects = objects;
        ContactListAdapter._itemLayout = itemLayout;
        ContactListAdapter._fieldId = fieldId;
		_activity = activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater =
					(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(ContactListAdapter._itemLayout, null);
		}

		if (_objects.isEmpty()) {
			return null;
		}

		String element = _objects.get(position);

		if (element != null) {
			TextView label = (TextView) v.findViewById(ContactListAdapter._fieldId);
			if (label != null) {
				label.setText(element);
			}
		}

		return v;
	}
}
