package com.mikal.chattingapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView  userProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private StorageReference UserProfileImegesRef;

    private static final int GalleryPick=1;
    private ProgressDialog loadingBar;
    private Toolbar settingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImegesRef= FirebaseStorage.getInstance().getReference().child("profile images");
        currentUserID=mAuth.getCurrentUser().getUid();

        initializeField();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateSettings();
            }
        });

        retrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);

            }
        });

    }



    private void initializeField() {

        updateAccountSettings=(Button) findViewById(R.id.update_settings_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);
        settingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {

            Uri imageUri=data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK)
            {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("please wait profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri=result.getUri();


                StorageReference filePath=UserProfileImegesRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful())
                        {

                            Toast.makeText(SettingsActivity.this, "profile image uploaded successfully...", Toast.LENGTH_SHORT).show();

                           final String downloadUrl=task.getResult().getDownloadUrl().toString();

                           RootRef.child("Users").child(currentUserID).child("image")
                                   .setValue(downloadUrl)
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {

                                           if (task.isSuccessful())
                                           {
                                               Toast.makeText(SettingsActivity.this, "image saved in database successfully...", Toast.LENGTH_SHORT).show();
                                               loadingBar.dismiss();

                                           }
                                           else
                                           {
                                               String message=task.getException().toString();
                                               Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                               loadingBar.dismiss();
                                           }

                                       }
                                   });
                        }

                        else
                        {

                            String message=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                });

            }


        }

    }

    private void updateSettings() {

        String setUserName=userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();


        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "please write your name first....", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(setUserStatus))
        {
            Toast.makeText(this, "please write your status first....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profileMap= new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {

                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated sucessfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }
    }


    private void retrieveUserInfo() {

        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {

                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus=dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);


                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {

                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "please set and update your ptofile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
