package fr.unix_experience.owncloud_sms.activities.remote_account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import fr.unix_experience.android_lib.AppCompatListActivity;
import fr.unix_experience.owncloud_sms.R;

public class AccountActionsActivity extends AppCompatListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_actions);

        ArrayList<String> itemList = new ArrayList<>();
        ArrayAdapter<String> adp = new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_dropdown_item_1line, itemList);
        setListAdapter(adp);

        // Create item list
        itemList.add(getBaseContext().getString(R.string.restore_all_messages));

        adp.notifyDataSetChanged();

        // Fetch account name from intent
        _accountName = getIntent().getStringExtra("account");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                Intent intent = new Intent(this, RestoreMessagesActivity.class);
                intent.putExtra("account", _accountName);
                startActivity(intent);
                break;
            default: break; // Unhandled
        }
    }

    private String _accountName = "";
    private static final String TAG = AccountActionsActivity.class.getSimpleName();
}
