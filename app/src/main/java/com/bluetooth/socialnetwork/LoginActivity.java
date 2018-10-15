package com.bluetooth.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText UserEmail,UserPaassword;
    private TextView NeedNewAccountLink;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth= FirebaseAuth.getInstance();


        NeedNewAccountLink = findViewById(R.id.register_account_link);
         UserEmail = findViewById(R.id.login_email);
         UserPaassword = findViewById(R.id.login_password);
         LoginButton = findViewById(R.id.login_button);
         loadingBar = new ProgressDialog(this);

         NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 SendUserToRegisterActivity();
             }
         });

         LoginButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 AllowingUserToLogin();
             }
         });
    }


  @Override
   protected void onStart() {
        super.onStart();

        FirebaseUser currentUser  = mAuth.getCurrentUser();
        if(currentUser != null){
            //if Currentuser is equal to null that means currentuser donot authenticate
            SendUserToMainActivity();

        }
    }

    private void AllowingUserToLogin() {
        String email  = UserEmail.getText().toString();
        String password = UserPaassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email....", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Write your password...", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Login...");
            loadingBar.setMessage("Please wait......");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "You are logged in Successfully.....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                String message = task.getException().getMessage();

                                Toast.makeText(LoginActivity.this, "Error Occured:"  +message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
       finish();
    }

    private void SendUserToRegisterActivity() {

        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
      //  finish();
    }
}
