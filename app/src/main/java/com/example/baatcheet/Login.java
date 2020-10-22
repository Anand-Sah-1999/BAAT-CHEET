package com.example.baatcheet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Login extends AppCompatActivity {

    TextInputEditText email, password;
    ProgressBar progressBar;
    Button loginButton;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if a user is already logged in, it will directly send the user in his profile
        if(auth.getCurrentUser()!=null){
            Toast.makeText(Login.this, "Logged in as : "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Login.this, TabbedActivity.class));
        }
        email.setText("");
        password.setText("");
    }

    private void setViews() {
        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        auth = FirebaseAuth.getInstance();
    }

    public void moveToRegisterActivity(View v){
        startActivity(new Intent(Login.this, Register.class));
    }

    public void login(View view) {
        final String emailString = email.getText().toString();
        final String passwordString = password.getText().toString();

        if (emailString.isEmpty()) {
            email.setError("E-mail field empty..!!");
        }
        if (passwordString.isEmpty()) {
            password.setError("Password field empty..!!");
        }
        if (!emailString.isEmpty() && !passwordString.isEmpty()) {

            loginButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            // storing the user login and password in database
            auth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        loginButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Logged in as : "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, TabbedActivity.class));
                        finish();
                    }
                    else{
                        loginButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Username/password incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public void resetPassword(View view){
        View view1 = LayoutInflater.from(this).inflate(R.layout.forgot_password_custom_alert_dialogue, null);
        final TextView editDetails = view1.findViewById(R.id.editDetails);
        Button resetPassword = view1.findViewById(R.id.resetPassword);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("We just need your registered email address to send you password reset link")
                .setView(view1).create().show();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editDetails.getText().toString().isEmpty()) {
                    auth.sendPasswordResetEmail(editDetails.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Login.this, "E-mail sent successfully. Please check your mail", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(Login.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(Login.this, "Please enter your registered email address first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
