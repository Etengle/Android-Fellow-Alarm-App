package netdb.courses.softwarestudio.labs.pushnotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Created by Slighten on 2015/1/20.
 */
public class ContactsActivity extends ActionBarActivity {
    private Cursor result = null; // all people
    private ListView contactsList = null;
    private List<Map<String,Object>> allContacts = null; // all data
    private SimpleAdapter simple = null;   // adapter
    private static String PhoneNumber = "0";
    public static final int REQUEST_CODE = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_contacts);
        Toast.makeText(this, "Long press to choose a contact person", Toast.LENGTH_LONG).show();
        this.contactsList = (ListView) super.findViewById(R.id.contactsList);
        // query
        this.result = super.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        super.startManagingCursor(this.result);
        // physical list
        this.allContacts = new ArrayList<Map <String, Object>>();
        // get data
        for (this.result.moveToFirst(); !this.result.isAfterLast(); this.result.moveToNext()) {
            Map <String, Object> contact = new HashMap<String, Object>();
            // set ID
            contact.put("_id", this.result.getInt(this.result.getColumnIndex(ContactsContract.Contacts._ID)));
            // set name
            contact.put("name", this.result.getString(this.result.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            // store map
            this.allContacts.add(contact);
        }
        // encapsule
        this.simple = new SimpleAdapter(this,
                this.allContacts,
                R.layout.contacts,
                new String[] { "name" },
                new int[] { R.id.name });
        this.contactsList.setAdapter(this.simple);
        super.registerForContextMenu(this.contactsList); // long press
        // contactsList.setOnItemClickListener(new OnItemClickListener());
    }

    // show
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);
        menu.setHeaderTitle("Contact");
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Choose it!");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        long contactsId = Long.parseLong(this.allContacts.get(position).get("_id").toString());
        switch (item.getItemId()){
            case Menu.FIRST + 1:
                String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
                String[] phoneSelectionArgs = { String.valueOf(contactsId)};
                Cursor c = super.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, phoneSelection, phoneSelectionArgs, null);
                StringBuffer buf = new StringBuffer();
                buf.append("You just chose: " + this.allContacts.get(position).get("name") + "\nHis/Her number is: ");
                /*for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
                    buf.append(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.
                            NUMBER))).append(", ");*/
                c.moveToFirst();
                PhoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                buf.append(PhoneNumber);
                Toast.makeText(this, buf, Toast.LENGTH_LONG).show();
                ContactsActivity.this.getIntent().putExtra("PhoneNumber", PhoneNumber);
                ContactsActivity.this.setResult(RESULT_OK, ContactsActivity.this.getIntent());
                ContactsActivity.this.finish();
                break;
        }
        return false;
    }

}
