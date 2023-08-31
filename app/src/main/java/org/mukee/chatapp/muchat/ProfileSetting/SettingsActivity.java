package org.mukee.chatapp.muchat.ProfileSetting;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.mukee.chatapp.muchat.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.hasnat.sweettoast.SweetToast;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profile_settings_image;
    private TextView display_status, updatedMsg, recheckGender;
    private ImageView editPhotoIcon, editStatusBtn;
    private EditText display_name, display_email, user_phone, user_profession, user_nickname;
    private RadioButton maleRB, femaleRB;

    private Button saveInfoBtn;

    private DatabaseReference getUserDatabaseReference;
    private FirebaseAuth mAuth;
    private StorageReference mProfileImgStorageRef;
    private StorageReference thumb_image_ref;

    private final static int GALLERY_PICK_CODE = 1;
    Bitmap thumb_Bitmap = null;

    private ProgressDialog progressDialog;
    private String selectedGender = "", profile_download_url, profile_thumb_download_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //setCropImageView(binding.cropImageView);


        mAuth = FirebaseAuth.getInstance();
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        getUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        getUserDatabaseReference.keepSynced(true); // for offline

        mProfileImgStorageRef = FirebaseStorage.getInstance().getReference().child("profile_image");
        thumb_image_ref = FirebaseStorage.getInstance().getReference().child("thumb_image");

        profile_settings_image = findViewById(R.id.profile_img);
        display_name = findViewById(R.id.user_display_name);
        user_nickname = findViewById(R.id.user_nickname);
        user_profession = findViewById(R.id.profession);
        display_email = findViewById(R.id.userEmail);
        user_phone = findViewById(R.id.phone);
        display_status = findViewById(R.id.userProfileStatus);
        editPhotoIcon = findViewById(R.id.editPhotoIcon);
        saveInfoBtn = findViewById(R.id.saveInfoBtn);
        editStatusBtn = findViewById(R.id.statusEdit);
        updatedMsg = findViewById(R.id.updatedMsg);

        recheckGender = findViewById(R.id.recheckGender);
        recheckGender.setVisibility(View.VISIBLE);


        maleRB = findViewById(R.id.maleRB);
        femaleRB = findViewById(R.id.femaleRB);


        Toolbar toolbar = findViewById(R.id.profile_settings_appbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);

        // Retrieve data from database
        getUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // retrieve data from db
                String name = Objects.requireNonNull(dataSnapshot.child("user_name").getValue()).toString();
                String nickname = Objects.requireNonNull(dataSnapshot.child("user_nickname").getValue()).toString();
                String profession = Objects.requireNonNull(dataSnapshot.child("user_profession").getValue()).toString();
                String status = Objects.requireNonNull(dataSnapshot.child("user_status").getValue()).toString();
                String email = Objects.requireNonNull(dataSnapshot.child("user_email").getValue()).toString();
                String phone = Objects.requireNonNull(dataSnapshot.child("user_mobile").getValue()).toString();
                String gender = Objects.requireNonNull(dataSnapshot.child("user_gender").getValue()).toString();
                final String image = Objects.requireNonNull(dataSnapshot.child("user_image").getValue()).toString();
                String thumbImage = Objects.requireNonNull(dataSnapshot.child("user_thumb_image").getValue()).toString();

                display_status.setText(status);

                display_name.setText(name);
                display_name.setSelection(display_name.getText().length());

                user_nickname.setText(nickname);
                user_nickname.setSelection(user_nickname.getText().length());

                user_profession.setText(profession);
                user_profession.setSelection(user_profession.getText().length());

                user_phone.setText(phone);
                user_phone.setSelection(user_phone.getText().length());

                display_email.setText(email);


                if (!image.equals("default_image")) { // default image condition for new user
                    Picasso.get()
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE) // for offline
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .into(profile_settings_image);
                }

                if (gender.equals("Male")) {
                    maleRB.setChecked(true);
                } else if (gender.equals("Female")) {
                    femaleRB.setChecked(true);
                } else {
                    maleRB.setChecked(false);
                    femaleRB.setChecked(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        /** Change profile photo from GALLERY */
        editPhotoIcon.setOnClickListener(v -> {
            // open gallery
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(galleryIntent, GALLERY_PICK_CODE);
        });

        /** Edit information */
        saveInfoBtn.setOnClickListener(v -> {
            String uName = display_name.getText().toString();
            String uNickname = user_nickname.getText().toString();
            String uPhone = user_phone.getText().toString();
            String uProfession = user_profession.getText().toString();

            saveInformation(uName, uNickname, uPhone, uProfession, selectedGender);
        });

        /** Edit STATUS */
        editStatusBtn.setOnClickListener(v -> {
            String previous_status = display_status.getText().toString();

            Intent statusUpdateIntent = new Intent(SettingsActivity.this, StatusUpdateActivity.class);
            // previous status from db
            statusUpdateIntent.putExtra("ex_status", previous_status);
            startActivity(statusUpdateIntent);
        });

        // hide soft keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    } // Ending onCrate

    // Gender Radio Button
    public void selectedGenderRB(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.maleRB:
                if (checked) {
                    selectedGender = "Male";
                    recheckGender.setVisibility(View.GONE);
                    break;
                }
            case R.id.femaleRB:
                if (checked) {
                    selectedGender = "Female";
                    recheckGender.setVisibility(View.GONE);
                    break;
                }
        }
    }


    private void saveInformation(String uName, String uNickname, String uPhone, String uProfession, String uGender) {
        if (uGender.length() < 1) {
            recheckGender.setTextColor(Color.RED);
            //Toasty.info(this, "To save changes, please recheck your GENDER", 1000).show();
        } else if (TextUtils.isEmpty(uName)) {
            SweetToast.error(this, "Oops! your name can't be empty");
        } else if (uName.length() < 3 || uName.length() > 40) {
            SweetToast.warning(this, "Your name should be 3 to 40 numbers of characters");
        } else if (TextUtils.isEmpty(uPhone)) {
            SweetToast.error(this, "Your mobile number is required.");
        } else if (uPhone.length() < 11) {
            SweetToast.warning(this, "Sorry! your mobile number is too short");
        } else {
            getUserDatabaseReference.child("user_name").setValue(uName);
            getUserDatabaseReference.child("user_nickname").setValue(uNickname);
            getUserDatabaseReference.child("search_name").setValue(uName.toLowerCase());
            getUserDatabaseReference.child("user_profession").setValue(uProfession);
            getUserDatabaseReference.child("user_mobile").setValue(uPhone);

            getUserDatabaseReference.child("user_gender").setValue(uGender)
                    .addOnCompleteListener(task -> {
                        updatedMsg.setVisibility(View.VISIBLE);

                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                SettingsActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        updatedMsg.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }, 1500);
                    }).addOnFailureListener(e -> {
                    });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** Cropping image functionality
         * Library Link- https://github.com/ArthurHub/Android-Image-Cropper
         * */
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri = data.getData();
        // start picker to get image for cropping and then use the image in cropping activity

        ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
            } else {
                //Exception error = result.getError();
                //handling more event
                SweetToast.info(SettingsActivity.this, "Image cropping failed.");
            }
        });

        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.imageSourceIncludeGallery = false;
        cropImageOptions.imageSourceIncludeCamera = true;
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(/*pass you image uri*/imageUri, cropImageOptions);
        cropImage.launch(cropImageContractOptions);


        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final Uri resultUri = cropImageContractOptions.getUri();

        File thumb_filePath_Uri = new File(resultUri.getPath());
        //File thumb_filePath_Uri = new File(imageUri.getPath());

        final String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


        // firebase storage for uploading the cropped image
        final StorageReference filePath = mProfileImgStorageRef.child(user_id + ".jpg");

        UploadTask uploadTask = filePath.putFile(resultUri);
        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (!task.isSuccessful()) {
                    SweetToast.error(SettingsActivity.this, "Profile Photo Error: " + Objects.requireNonNull(task.getException()).getMessage());
                    //throw task.getException();
                }
                profile_download_url = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SweetToast.info(SettingsActivity.this, "Your profile photo is uploaded successfully.", Toast.LENGTH_SHORT);
                // retrieve the stored image as profile photo
                profile_download_url = task.getResult().toString();
                Log.e("tag", "profile url: " + profile_download_url);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                thumb_Bitmap.compress(Bitmap.CompressFormat.JPEG, 45, outputStream);
                final byte[] thumb_byte = outputStream.toByteArray();

                // firebase storage for uploading the cropped and compressed image
                final StorageReference thumb_filePath = thumb_image_ref.child(user_id + "jpg");
                UploadTask thumb_uploadTask = thumb_filePath.putBytes(thumb_byte);

                Task<Uri> thumbUriTask = thumb_uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (!task.isSuccessful()) {
                            SweetToast.error(SettingsActivity.this, "Thumb Image Error: " + task.getException().getMessage());
                        }
                        profile_thumb_download_url = thumb_filePath.getDownloadUrl().toString();
                        return thumb_filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        profile_thumb_download_url = task.getResult().toString();
                        Log.e("tag", "thumb url: " + profile_thumb_download_url);
                        if (task.isSuccessful()) {
                            Log.e("tag", "thumb profile updated");

                            HashMap<String, Object> update_user_data = new HashMap<>();
                            update_user_data.put("user_image", profile_download_url);
                            update_user_data.put("user_thumb_image", profile_thumb_download_url);

                            getUserDatabaseReference.updateChildren(new HashMap<String, Object>(update_user_data))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("tag", "thumb profile updated");
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("tag", "for thumb profile: " + e.getMessage());
                                            progressDialog.dismiss();
                                        }
                                    });
                        }

                    }
                });


                thumb_uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        SweetToast.warning(SettingsActivity.this, "Error occurred!! " + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot != null) {
                            profile_thumb_download_url = String.valueOf(taskSnapshot.getMetadata().getReference().getDownloadUrl());
                            Log.e("tag", "profile_thumb_download_url: " + profile_thumb_download_url);

                            HashMap<String, Object> update_user_data = new HashMap<>();
                            update_user_data.put("user_image", profile_download_url);
                            update_user_data.put("user_thumb_image", profile_thumb_download_url);

                            getUserDatabaseReference.updateChildren(update_user_data)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("tag", "for thumb profile: " + e.getMessage());
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("tag", "thumb profile updated");
                                            progressDialog.dismiss();
                                            SweetToast.success(SettingsActivity.this, "Profile photo is updated successfully.", Toast.LENGTH_SHORT);
                                        }
                                    });
                        }

                    }
                });

                //StorageTask<UploadTask.TaskSnapshot> taskSnapshotStorageTask = uploadTask.addOnCompleteListener;

            }


        });


    }


}