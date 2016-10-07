package fr.unix_experience.owncloud_sms.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fr.unix_experience.owncloud_sms.R;

public class RecoveryPhoneNumberListViewAdapter extends ArrayAdapter<String> {
    private static final String TAG = "RecPhoneNumberListVAdp";
    private static int _fieldId = R.id.recovery_phone;
    private static int _itemLayout = R.layout.recovery_phone_list_item;
    private static int resource = android.R.layout.simple_list_item_2;

    public RecoveryPhoneNumberListViewAdapter(Context context) {
        super(context, resource, new ArrayList<String>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(RecoveryPhoneNumberListViewAdapter._itemLayout, null);
        }

        TextView label = (TextView) v.findViewById(RecoveryPhoneNumberListViewAdapter._fieldId);
        if (label != null) {
            final String l = getItem(position).toString();
            label.setText(getItem(position).toString());
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Clicked on phone " + l);
                }
            });
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.i(TAG, "Long clicked on phone " + l);
                    return false;
                }
            });

        }

        return v;
    }
}
