package com.bluetooth.socialnetwork;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
//import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
   private TextView NavProfileUserName;

   private ImageButton AddNewPostButton;
    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;
  String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            mAuth = FirebaseAuth.getInstance();

            try {

                currentUserID = mAuth.getCurrentUser().getUid();
            }catch (Exception ex){
                ex.printStackTrace();
            }

            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        //Added Toolbar in mainactivity
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        //we have to assign toolbar over mainactivity
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = findViewById(R.id.add_new_post_button);



        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle =new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigation_view);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        //Including navigation header on navigation drawer
        //we are storing navigation header inside layout
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        NavProfileImage = navView. findViewById(R.id.nav_profile_image);

        NavProfileUserName = navView. findViewById(R.id.nav_user_full_name);

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("fullname")){
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);

                    }
                    if(dataSnapshot.hasChild("Profile Image")){
                        String image = dataSnapshot.child("Profile Image").getValue().toString();


                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }else{
                        Toast.makeText(MainActivity.this, "Profile Name donot exist", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendUserPostActivity();
            }
        });

        DisplayAllUsersPost();
    }

    private void DisplayAllUsersPost() {

    }

    private void SendUserPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
    }

    @Override
    //when our app run then onStart method call automatically
    protected void onStart() {
        super.onStart();

        // currentonline firebaseuser who uses our app
        FirebaseUser currentUser  = mAuth.getCurrentUser();
        if(currentUser == null){
            //if Currentuser is equal to null that means currentuser donot authenticate
           SendUserTologinActivity();

        }else{

            CheckUserExistence();
        }
    }

private void CheckUserExistence(){

        final String current_user_id = mAuth.getCurrentUser().getUid();


        // Attach a listener to read the data at our posts reference
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
}

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);

        //These are basically the validation
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserTologinActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);

        //These are basically the validation
          loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override

    //Action perform on ActionBarDrawerToggle open and close
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        //Here is the item selected by user
        switch (item.getItemId()){

            case R.id.nav_post:
              SendUserPostActivity();
                break;

            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this, "FriendList", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                Toast.makeText(this, "Find_friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                  mAuth.signOut();
                  SendUserTologinActivity();
                break;
        }
    }
}
