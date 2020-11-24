package com.klmni.la8eni.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.klmni.la8eni.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

    private Toolbar settingToolbar;

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;


    private  static final int GALLERY_PICK = 1;

    private String downloadUrl;
    private String currentUserID;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference RootReference;
    private StorageReference userProfileImageReference;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();

        initializeDatabase();

        userName.setVisibility(View.INVISIBLE);

        clickedViews();

        retrieveUserInformation();

    }

    private void retrieveUserInformation()
    {
        RootReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")))
                {
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserStatus = snapshot.child("status").getValue().toString();
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                    Picasso.get()
                            .load(retrieveProfileImage)
                            .into(userProfileImage);

                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name")))
                {
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserStatus = snapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                }
                else
                {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please update your profile information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(SettingsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews()
    {
        settingToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        userProfileImage = findViewById(R.id.set_profile_image);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        UpdateAccountSettings = findViewById(R.id.update_setting_button);

        loadingBar = new ProgressDialog(SettingsActivity.this);
    }

    private void initializeDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference();
        userProfileImageReference = FirebaseStorage.getInstance().getReference();
    }

    private void clickedViews()
    {
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                UpdateSetting();
            }
        });


        userProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                checkRequestPermission();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait.., Your profile image is updating");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);

            Uri photoPathUri = activityResult.getUri();

            final StorageReference filePath = userProfileImageReference.child("Profile Images").child(currentUserID /*+ ".jpg" */);


            final UploadTask task = filePath.putFile(photoPathUri);
            task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        loadingBar.dismiss();

                        Uri downloadUri = task.getResult();

                        downloadUrl = downloadUri.toString();

                        RootReference.child("Users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Image Save In Database, Done", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
            /*.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(SettingsActivity.this, "Profile Image Uploaded", Toast.LENGTH_SHORT).show();


                        final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                        RootReference.child("Users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Image Save In Database, Done", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        loadingBar.dismiss();
                        Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }); */

            Picasso.get()
                    .load(photoPathUri)
                    .error(R.drawable.profile_image)
                    .into(userProfileImage);

        }

    }

    private void UpdateSetting()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write your user name", Toast.LENGTH_SHORT).show();
            userName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
            userStatus.requestFocus();
            return;
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            RootReference.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity()
    {
        Intent loginIntent = new Intent(SettingsActivity.this , MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void checkRequestPermission()
    {
        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_PICK);
        }
        else
        {
            openGallery();
        }
    }

    private void openGallery()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1   , 1)
                .start(SettingsActivity.this);
    }

}