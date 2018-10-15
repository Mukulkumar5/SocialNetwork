package com.bluetooth.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static  final  int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Description;
    private StorageReference PostImagesReference;
    private DatabaseReference UserRef, PostsRef;
    private FirebaseAuth mAuth;
    private  String saveCurrentDate, saveCurrentTime, postRandomName,downloadUrl, current_use_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth = FirebaseAuth.getInstance();
        current_use_id = mAuth.getUid();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        PostImagesReference = FirebaseStorage.getInstance().getReference();//The entry point to accessing firebade storage you can get instance by calling getInstance and to access location byb calling getReference()
        SelectPostImage = (ImageButton)findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);//Here error occur, i remove this error by adding support.v7 in import Toolbar

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {

       Description = PostDescription.getText().toString();

        if (ImageUri == null) {
            Toast.makeText(this, "please select post image...", Toast.LENGTH_SHORT).show();

        }
          else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "please say about your image...", Toast.LENGTH_SHORT).show();

        } else{
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait......");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();

        }
    }

    private void StoringImageToFirebaseStorage() {

        //Image store in firebase

        Calendar callForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");

        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");

        saveCurrentTime = currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;



        StorageReference filePath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");//from first child we create a folder and in second we store the post image of the user

        filePath.putFile(ImageUri)/*image upload in firebase database*/.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() { //This check image upload successfully or not
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful())
                {
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "image uploaded successfully...", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();

                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {

        UserRef.child(current_use_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("Profile Image").getValue().toString();

                    HashMap postsMap = new HashMap();

                    postsMap.put("uid",current_use_id);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",Description);
                    postsMap.put("postimage",downloadUrl);
                    postsMap.put("profile image",userProfileImage);
                    postsMap.put("full Name",userFullName);
                    PostsRef.child(current_use_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Plost Is Updated successful", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                Toast.makeText(PostActivity.this, "ErrorOccured....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void OpenGallery() {

        //when click on profile redirect on gallery



            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
           startActivityForResult(galleryIntent, Gallery_Pick);//pick a picture from gallery
       // startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"),Gallery_Pick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
          ImageUri = data.getData();
          SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent  );
    }
}

