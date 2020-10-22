package com.example.baatcheet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {

    CircleImageView profile_image2;
    TextView name, status;
    FirebaseAuth auth;
    static Uri resultUri;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    FloatingActionButton fab3;
    ImageView showFullImage, showBlackBackground;
    RelativeLayout relativeLayout1, relativeLayout2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setViews();
        set_User_Info_in_Views_From_Firebase();

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfilePic();
            }
        });
    }

    private void setViews() {
        showFullImage = findViewById(R.id.showFullImage);
        showBlackBackground = findViewById(R.id.showBlackBackground);
        profile_image2 = findViewById(R.id.profile_image2);
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fab3 = findViewById(R.id.fab3);
        relativeLayout1 = findViewById(R.id.relativeLayout1);
        relativeLayout2 = findViewById(R.id.relativeLayout2);

    }

    private void set_User_Info_in_Views_From_Firebase() {

        String uniqueUserID = auth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(uniqueUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Picasso.with(Settings.this).load(documentSnapshot.getString("Image URL")).placeholder(R.drawable.default_dp).into(profile_image2);
                name.setText(documentSnapshot.getString("Name"));
                status.setText(documentSnapshot.getString("Status"));
            }
        });
    }

    public void editName(View view){

        View view1 = LayoutInflater.from(this).inflate(R.layout.edit_info_custom_alert_dialogue, null);
        final TextView editDetails = view1.findViewById(R.id.editDetails);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Display Name").setView(view1).setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("Name", editDetails.getText().toString());
                firebaseFirestore.collection("Users").document(auth.getCurrentUser().getUid()).set(map, SetOptions.merge());

                // add changed name data in the current users database, so that he can see the other users changed name
                firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                            final String receiverUserID = snapshot.getString("User ID");
                            firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists() && !receiverUserID.equals(auth.getCurrentUser().getUid())) {
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("Name", editDetails.getText().toString());
                                        firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).set(map, SetOptions.merge());
                                    }
                                }
                            });
                        }
                    }
                });

                set_User_Info_in_Views_From_Firebase();         // To Update the results in textView as soon the user clicks SAVE
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void editStatus(View view){
        View view1 = LayoutInflater.from(this).inflate(R.layout.edit_info_custom_alert_dialogue, null);
        final TextView editDetails = view1.findViewById(R.id.editDetails);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Status").setView(view1).setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("Status", editDetails.getText().toString());
                firebaseFirestore.collection("Users").document(auth.getCurrentUser().getUid()).set(map, SetOptions.merge());

                set_User_Info_in_Views_From_Firebase();     // To Update the results in textView as soon the user clicks SAVE
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
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
                Settings.resultUri = result.getUri();
                profile_image2.setImageURI(resultUri);

                if(resultUri!=null) {
                    storageReference.child("User DP").child(auth.getCurrentUser().getUid()).putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.child("User DP").child(auth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("Image URL", uri.toString());
                                    firebaseFirestore.collection("Users").document(auth.getCurrentUser().getUid()).set(map, SetOptions.merge());

                                    firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                                                final String receiverUserID = snapshot.getString("User ID");
                                                firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if(documentSnapshot.exists() && !receiverUserID.equals(auth.getCurrentUser().getUid())) {
                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put("Image URL", uri.toString());
                                                            firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).set(map, SetOptions.merge());
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });

                                }
                            });
                        }
                    });
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void displayFullImage(View view){
        fab3.setVisibility(View.INVISIBLE);
        showFullImage.setVisibility(View.VISIBLE);
        showBlackBackground.setVisibility(View.VISIBLE);
        fab3.setEnabled(false);
        relativeLayout1.setEnabled(false);
        relativeLayout2.setEnabled(false);

        firebaseFirestore.collection("Users").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Picasso.with(Settings.this).load(documentSnapshot.getString("Image URL")).placeholder(R.drawable.loading_icon).into(showFullImage);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (showFullImage.getVisibility()==View.VISIBLE){

            fab3.setEnabled(true);
            relativeLayout1.setEnabled(true);
            relativeLayout2.setEnabled(true);
            fab3.setVisibility(View.VISIBLE);
            showFullImage.setVisibility(View.INVISIBLE);
            showBlackBackground.setVisibility(View.INVISIBLE);
        }
        else{
            super.onBackPressed();
        }
    }
}
