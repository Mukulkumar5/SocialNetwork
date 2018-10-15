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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail,UserPassword,UserConfirmPassword;
    private Button CreateAccountButton;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        UserConfirmPassword = findViewById(R.id.register_confirmpassword);
        CreateAccountButton = findViewById(R.id.register_create_account);

        loadingBar = new ProgressDialog(this);
        
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              CreateNewAccount();
            }
        });
    }

   @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //if Currentuser is equal to null that means currentuser donot authenticate
            SendUserToMainActivity();

        }
    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount() {
        
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmpassword = UserConfirmPassword.getText().toString();
        
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please write your email....", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(confirmpassword)){
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show();
        }else if(! password.equals(confirmpassword)){
            Toast.makeText(this, "Your password donot match with your confirm password", Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait......");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendUserToActivity();
                                Toast.makeText(RegisterActivity.this, "you are authenticated successfully.....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error occured:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });

        }
        
    }

    private void SendUserToActivity() {

        Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
