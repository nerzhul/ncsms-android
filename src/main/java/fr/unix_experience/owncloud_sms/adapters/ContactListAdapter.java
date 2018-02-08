package fr.unix_experience.owncloud_sms.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fr.unix_experience.owncloud_sms.R;

public class ContactListAdapter extends ArrayAdapter<String> {
	private final ArrayList<String> _objects;

    // Design
    private final static int _itemLayout = R.layout.contact_list_item;
    private final static int _fieldId = R.id.contactname;
	
	public ContactListAdapter(Context context, ArrayList<String> objects) {
		super(context, android.R.layout.simple_spinner_item, objects);
		_objects = objects;
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
			TextView label = v.findViewById(ContactListAdapter._fieldId);
			if (label != null) {
				label.setText(element);
			}
		}

		return v;
	}
}
