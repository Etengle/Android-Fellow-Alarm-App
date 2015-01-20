package netdb.courses.softwarestudio.labs.pushnotification;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Map;

import netdb.course.softwarestudio.service.rest.RestManager;
/**
 * Created by Slighten on 2015/1/19.
 */
public class PostUserActivity extends Activity {

    private EditText name;
    private EditText nickname;
    private Button PostBtn;

    private RestManager mRestMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user);

        // Create a REST manager instance to do REST methods
        mRestMgr = RestManager.getInstance(getApplication());


        // TODO: Initialize UI views
        name = (EditText) findViewById(R.id.editText);
        nickname = (EditText) findViewById(R.id.editText2);
        PostBtn = (Button) findViewById(R.id.button);
        // TODO: Set button listener for posting message
        PostBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                postUser(name.getText().toString(), nickname.getText().toString());
            }
        });
    }

    /**
     * Create a new user
     *
     * @param name The user name
     * @param nickname  The user nickname
     */
    private void postUser(String name, String nickname) {
        final User user = new User();
        user.setName(name);
        user.setId(MainActivity.regId);

        mRestMgr.postResource(User.class, user, new RestManager.PostResourceListener() {
            @Override
            public void onResponse(int code, Map<String, String> headers) {
                // TODO: Finish this activity
                finish();
            }

            @Override
            public void onRedirect(int code, Map<String, String> headers, String url) {
                onError(null, null, code, headers);
            }

            @Override
            public void onError(String message, Throwable cause, int code, Map<String, String> headers) {
                Log.d(this.getClass().getSimpleName(), "" + code + ": " + message);
                finish();
            }
        }, null);
    }


}
