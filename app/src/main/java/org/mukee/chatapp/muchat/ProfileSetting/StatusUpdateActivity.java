package org.mukee.chatapp.muchat.ProfileSetting;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mukee.chatapp.muchat.R;

import java.util.Objects;

import xyz.hasnat.sweettoast.SweetToast;

public class StatusUpdateActivity extends AppCompatActivity {

    private static final String TAG = "StatusUpdateActivity";

    private EditText status_from_input;
    private ProgressDialog progressDialog;

    private DatabaseReference statusDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        statusDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        status_from_input = findViewById(R.id.input_status);
        progressDialog = new ProgressDialog(this);

        Toolbar mToolbar = findViewById(R.id.update_status_appbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // back on previous activity
        mToolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "onClick : navigating back to 'SettingsActivity.class' ");
            finish();
        });

        /*
          retrieve previous profile status from SettingsActivity
         */
        String previousStatus = getIntent().getExtras().get("ex_status").toString();
        status_from_input.setText(previousStatus);
        status_from_input.setSelection(status_from_input.getText().length());
    } //ending onCreate

    // tool bar Status update done- menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.update_status_done_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.status_update_done){
            String new_status = status_from_input.getText().toString();
            changeProfileStatus(new_status);
        }
        return true;
    }

    private void changeProfileStatus(String new_status) {
        if (TextUtils.isEmpty(new_status)){
            SweetToast.warning(getApplicationContext(), "Please write something about status");
        } else {
            progressDialog.setMessage("Updating status...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            statusDatabaseReference.child("user_status").setValue(new_status)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            finish();
                        } else {
                            SweetToast.warning(getApplicationContext(), "Error occurred: failed to update.");
                        }
                    });
        }
    }

}
