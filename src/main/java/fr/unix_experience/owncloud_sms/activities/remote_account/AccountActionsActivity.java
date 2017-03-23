package fr.unix_experience.owncloud_sms.activities.remote_account;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import fr.unix_experience.android_lib.AppCompatListActivity;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

public class AccountActionsActivity extends AppCompatListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_actions);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ArrayList<String> itemList = new ArrayList<>();
        ArrayAdapter<String> adp = new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_dropdown_item_1line, itemList);
        setListAdapter(adp);

        // Create item list
        itemList.add(getBaseContext().getString(R.string.restore_all_messages));
		itemList.add(getBaseContext().getString(R.string.reinit_sync_cursor));

        adp.notifyDataSetChanged();

        // Fetch account name from intent
        _accountName = getIntent().getStringExtra("account");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval = true;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                retval = super.onOptionsItemSelected(item);
        }
        return retval;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                Intent intent = new Intent(this, RestoreMessagesActivity.class);
                intent.putExtra("account", _accountName);
				try {
					startActivity(intent);
				}
				catch (IllegalStateException e) {
					Log.e(AccountActionsActivity.TAG, e.getMessage());
				}
                break;
			case 1:
				final Context me = this;
				new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.reinit_sync_cursor)
						.setMessage(R.string.reinit_sync_cursor_confirm)
						.setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								(new OCSMSSharedPrefs(me)).setLastMessageDate(0L);
								Log.i(AccountActionsActivity.TAG, "Synchronization cursor reinitialized");
							}

						})
						.setNegativeButton(R.string.no_confirm, null)
						.show();
				break;
            default: break; // Unhandled
        }
    }

    private String _accountName = "";
    private static final String TAG = AccountActionsActivity.class.getSimpleName();
}
