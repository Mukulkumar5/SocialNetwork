package com.bluetooth.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
 import android.support.v7.widget.*;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName,FullName,CountryName;

    private Button SaveInformationbutton;
    private CircleImageView ProfileImage;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;

    String  currentUserID;
    final static  int Gallery_Pick = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

             mAuth = FirebaseAuth.getInstance();


        // try {

             currentUserID = mAuth.getCurrentUser().getUid();//Give Unique Id
             UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);//user refer to firebase database
             UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
       //  }catch(Exception ex){
        //     ex.printStackTrace();
       //  }


        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_fullname);
        CountryName = findViewById(R.id.setup_country_name);
        ProfileImage = findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);


        SaveInformationbutton = findViewById(R.id.setup_information_button);


        SaveInformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when click on profile redirect on gallery

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);//pick a picture from gallery
            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Profile Image")){
                        String image = dataSnapshot.child("Profile Image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);

                    }else{
                        Toast.makeText(SetupActivity.this, "Please select profile Image first", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            //once the user in gallery he will be redirected in the gallery
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        //get the crop image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait......");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                Uri resultUri = result.getUri();//get Uri
                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if(task.isSuccessful()){
                           Toast.makeText(SetupActivity.this, "Profile Image Store Successfully.....", Toast.LENGTH_SHORT).show();

                           //save the link of image inside the firebasedatabase
                           final String downloadUrl = task.getResult().getDownloadUrl().toString();

                           UserRef.child("Profile Image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()){

                                       Intent selfIntent = new Intent(SetupActivity.this,SetupActivity.class);
                                       startActivity(selfIntent);
                                       Toast.makeText(SetupActivity.this, "Profile Image stored to File....", Toast.LENGTH_SHORT).show();
                                  loadingBar.dismiss();

                                   }else{
                                       String message = task.getException().getMessage();
                                       Toast.makeText(SetupActivity.this, "Error Occured.....", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                   }
                               }
                           });


                       }
                    }
                });

            }else{
                Toast.makeText(this, "Error Occured: Image cannot be cropped...", Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation()
    {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String Country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username.......", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please write your fullname......", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Country)){
            Toast.makeText(this, "Please write your Country.......", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Saving Information.....");
            loadingBar.setMessage("Please wait......");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("Country",Country);
            userMap.put("status","Hey there, I am Poster Social network");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");
            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                 if(task.isSuccessful()){
                     SendUserToMainActivity();
                     Toast.makeText(SetupActivity.this, "Your Account is Created successfully", Toast.LENGTH_LONG).show();
                     loadingBar.dismiss();
                 }else{
                     String message = task.getException().getMessage();
                     Toast.makeText(SetupActivity.this, "Error Occured: " +message, Toast.LENGTH_SHORT).show();
                     loadingBar.dismiss();
                 }
                }
            });
        }


    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
