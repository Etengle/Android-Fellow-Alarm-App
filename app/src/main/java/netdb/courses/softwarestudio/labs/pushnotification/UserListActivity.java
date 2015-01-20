package netdb.courses.softwarestudio.labs.pushnotification;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netdb.course.softwarestudio.service.rest.RestManager;
/**
 * Created by Slighten on 2015/1/20.
 */
public class UserListActivity extends Activity {
    private ArrayList<User> uUserList = new ArrayList<User>();
    private ListView uListView;
    private UserAdapter uUserAdapter;

    private RestManager restMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Initialize views
        uListView = (ListView) findViewById(R.id.list_users);

        // Create a custom userAdapter and set to the list view
        uUserAdapter = new UserAdapter(this, uUserList);
        uListView.setAdapter(uUserAdapter);

        // Create a REST manager instance to do REST methods
        restMgr = RestManager.getInstance(getApplication());

        // Start a thread to get the messages from server.
        // After getting the messages, notify the UI thread to
        // redraw the UI view
        getUsers();

    }

    /**
     * Get all messages from server sorted by timestamp in desc order.
     */
    private void getUsers() {

        Map<String, String> params = new HashMap<String, String>();
        restMgr.listResource(User.class, params, new RestManager.ListResourceListener<User>() {
            @Override
            public void onResponse(int code, Map<String, String> headers, List<User> resources) {
                if (resources != null) {

                    uUserList.clear();
                    for(User m : resources){
                        uUserList.add(m);
                    }

                    UserListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           uUserAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onRedirect(int code, Map<String, String> headers, String url) {
                onError(null, null, code, headers);
            }

            @Override
            public void onError(String message, Throwable cause, int code,
                                Map<String, String> headers) {
                Log.d(this.getClass().getSimpleName(), "" + code + ": " + message);
            }
        }, null);
    }

}
