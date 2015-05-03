package fr.unix_experience.owncloud_sms.activities;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import fr.unix_experience.owncloud_sms.R;

public class RestoreSMSAccountListActivity extends ListActivity {
	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.restore_activity_accountlist);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				listItems);
		setListAdapter(adapter);

		listItems.add("test");
		listItems.add("test2");
		listItems.add("test3");
		listItems.add("test4s");
		adapter.notifyDataSetChanged();
	}
}

