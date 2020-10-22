package com.example.baatcheet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    TextInputEditText email, password, displayName;
    Button registerButton;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    CircleImageView profile_image;
    Uri resultUri;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setViews();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfilePic();
            }
        });
    }

    private void setViews() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        displayName = findViewById(R.id.displayName);
        registerButton = findViewById(R.id.registerButton);
        profile_image = findViewById(R.id.profile_image);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        floatingActionButton = findViewById(R.id.fab2);
    }

    public void moveToLoginActivity(View v){
        startActivity(new Intent(Register.this, Login.class));
        finish();
    }

    public void uploadProfilePic(){

    //  The uploadProfilePic and onActivityResult methods are copied from https://github.com/ArthurHub/Android-Image-Cropper
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    //  The uploadProfilePic and onActivityResult methods are copied from https://github.com/ArthurHub/Android-Image-Cropper
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profile_image.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void register(View view){

        final String emailString = email.getText().toString();
        final String passwordString = password.getText().toString();
        final String displayNameString = displayName.getText().toString();

        if(emailString.isEmpty()){
            email.setError("email field empty..!!");
        }
        if(passwordString.isEmpty()){
            password.setError("Password field empty..!!");
        }
        if(displayNameString.isEmpty()){
            displayName.setError("Display name field empty..!!");
        }
        if(!emailString.isEmpty() && !passwordString.isEmpty() && !displayNameString.isEmpty()){

            auth.createUserWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        String uniqueUserID = auth.getCurrentUser().getUid();

                        add_userInfo_To_FireStore_And_Storage(uniqueUserID,displayNameString);

                        startActivity(new Intent(Register.this, Login.class));

                    }
                    else{
                        Toast.makeText(Register.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void add_userInfo_To_FireStore_And_Storage(final String uniqueUserID, String displayNameSting) {

        if(resultUri!=null) {

            // upload data to firebase Storage, and get its "download URL"
            storageReference.child("User DP").child(uniqueUserID).putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageReference.child("User DP").child(uniqueUserID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("Image URL", uri.toString());

                            // store the image URL in the users database
                            firebaseFirestore.collection("Users").document(uniqueUserID).set(map, SetOptions.merge());
                        }
                    });
                }
            });
        }
        else{
            // if no profile pic is uploaded, store "null" under the "Image URL" key of the users database
            HashMap<String, Object> map = new HashMap<>();
            map.put("Image URL", "null");
            firebaseFirestore.collection("Users").document(uniqueUserID).set(map, SetOptions.merge());
        }

        // add other data like his name, unique ID and status in his database
        HashMap<String, Object> map = new HashMap<>();
        map.put("User ID",uniqueUserID);
        map.put("Name", displayNameSting);
        map.put("Status", "Hey there..!! Lets be friends");
        firebaseFirestore.collection("Users").document(uniqueUserID).set(map, SetOptions.merge());
    }
}