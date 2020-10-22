package com.example.baatcheet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class Story_Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RelativeLayout myStoryLayout;
    CoordinatorLayout friendsStoryLayout;
    FloatingActionButton addStoryFloatingButton;
    Uri resultUri;
    CircleImageView story_pic;
    ImageView showBlackBackground, showFullImage, backButton, deleteStory;
    TextView dateAndTime;
    FirebaseAuth auth; FirebaseStorage storage; FirebaseDatabase database; FirebaseFirestore firestore;
    String currentUser;
    RecyclerView storyRecyclerView;
    LinearLayoutManager linearLayoutManager;
    StoryFragment_AdapterClass storyFragmentAdapterClass;

    public Story_Fragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Story_Fragment newInstance(String param1, String param2) {
        Story_Fragment fragment = new Story_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_story_, container, false);

        setViews(view);
        retrieveStoryIfExistAndPutItInImageView();
        setUpRecyclerView();
        uploadStatus();

        return view;
    }

    private void setViews(View view) {

        addStoryFloatingButton = view.findViewById(R.id.addStoryFloatingButton);
        story_pic = view.findViewById(R.id.story_pic);
        showBlackBackground = view.findViewById(R.id.showBlackBackground);
        backButton = view.findViewById(R.id.backButton);
        deleteStory = view.findViewById(R.id.deleteStory);
        showFullImage = view.findViewById(R.id.showFullImage);
        dateAndTime = view.findViewById(R.id.dateAndTime);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storyRecyclerView = view.findViewById(R.id.storyRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        storyRecyclerView.setLayoutManager(linearLayoutManager);
        myStoryLayout = view.findViewById(R.id.myStoryLayout);
        friendsStoryLayout = view.findViewById(R.id.friendsStoryLayout);
    }


    private void retrieveStoryIfExistAndPutItInImageView() {

        // if the current user have an uploaded story, then put in in the image view when the app starts
        firestore.collection(currentUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    if(snapshot.exists()) {
                        if (snapshot.getString("Request Type").equals("friends")) {
                            firestore.collection(snapshot.getId()).document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable final DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (documentSnapshot.exists()) {
                                        if (!documentSnapshot.getString("Story URL").equals("Null")) {
                                            Picasso.with(getContext()).load(documentSnapshot.getString("Story URL")).placeholder(R.drawable.default_dp).fit().into(story_pic);
                                            dateAndTime.setText(documentSnapshot.getString("Story upload time"));

                                            // open full screen image when user clicks on his story
                                            myStoryLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                // if story is opened in full screen view, then prevent other stories to be clicked
                                                if(showBlackBackground.getVisibility()==View.VISIBLE){
                                                    myStoryLayout.setEnabled(false);
                                                    friendsStoryLayout.setEnabled(false);
                                                }
                                                else {
                                                    myStoryLayout.setEnabled(true);
                                                    friendsStoryLayout.setEnabled(true);
                                                    showBlackBackground.setVisibility(View.VISIBLE);
                                                    showFullImage.setVisibility(View.VISIBLE);
                                                    backButton.setVisibility(View.VISIBLE);
                                                    Picasso.with(getContext()).load(documentSnapshot.getString("Story URL")).placeholder(R.drawable.default_dp).fit().into(showFullImage);
                                                }
                                                }
                                            });

                                            // close the full screen image
                                            backButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (backButton.getVisibility() == View.VISIBLE) {
                                                        showBlackBackground.setVisibility(View.GONE);
                                                        showFullImage.setVisibility(View.GONE);
                                                        backButton.setVisibility(View.GONE);
                                                    }
                                                }
                                            });

                                            // delete story of current user from the database of all other users who are current users friend
                                            deleteStory.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                                    alert.setTitle("Delete Story?").setMessage("Are you sure, you want to delete your story?")
                                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.cancel();
                                                                }
                                                            }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            firestore.collection(currentUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                                        if (snapshot.getString("Request Type").equals("friends")) {

                                                                            Map<String, Object> map = new HashMap<>();
                                                                            map.put("Story URL", "Null");
                                                                            map.put("Story upload time", "Null");
                                                                            firestore.collection(snapshot.getId()).document(currentUser).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(getContext(), "Story Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                                                        Picasso.with(getContext()).load(R.drawable.default_dp).placeholder(R.drawable.default_dp).fit().into(story_pic);
                                                                                        dateAndTime.setText("Tap to add story");
                                                                                    } else {
                                                                                        Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).create().show();
                                                }
                                            });
                                        }
                                        else {
                                            // if current user does not have any story uploaded
                                            // clicking on "My Story" will open camera, so that user can add a story
                                            myStoryLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CropImage.activity()
                                                            .setGuidelines(CropImageView.Guidelines.ON)
//                                                          .setAspectRatio(1,1)
                                                            .start(getContext(), Story_Fragment.this);
                                                }
                                            });

                                            // if current user does not have any story uploaded
                                            // clicking on "deleteStory" will open camera, so that user can add a story
                                            deleteStory.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CropImage.activity()
                                                            .setGuidelines(CropImageView.Guidelines.ON)
                                                            .start(getContext(), Story_Fragment.this);
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setUpRecyclerView() {
        Query query = firestore.collection(currentUser).whereGreaterThan("Story URL", "Null");
        FirestoreRecyclerOptions<FindFriends_GetterSetter> options =
                new FirestoreRecyclerOptions.Builder<FindFriends_GetterSetter>()
                        .setQuery(query, FindFriends_GetterSetter.class)
                        .build();

        storyFragmentAdapterClass = new StoryFragment_AdapterClass(getContext(), options);
        storyRecyclerView.setAdapter(storyFragmentAdapterClass);

        storyFragmentAdapterClass.setClickedCardPosition(new StoryFragment_AdapterClass.GetClickedCardPosition() {
            @Override
            public void getPosition(DocumentSnapshot documentSnapshot) {

                // if story is opened in full screen view, then prevent other stories to be clicked
                if(showBlackBackground.getVisibility()==View.VISIBLE){
                    myStoryLayout.setEnabled(false);
                    friendsStoryLayout.setEnabled(false);
                }
                else {
                    myStoryLayout.setEnabled(true);
                    friendsStoryLayout.setEnabled(true);
                    showBlackBackground.setVisibility(View.VISIBLE);
                    showFullImage.setVisibility(View.VISIBLE);
                    backButton.setVisibility(View.VISIBLE);
                    Picasso.with(getContext()).load(documentSnapshot.getString("Story URL")).placeholder(R.drawable.default_dp).fit().into(showFullImage);
                }
            }
        });

        // close the full screen image
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backButton.getVisibility() == View.VISIBLE) {
                    showBlackBackground.setVisibility(View.GONE);
                    showFullImage.setVisibility(View.GONE);
                    backButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void uploadStatus() {
        addStoryFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1,1)
                    .start(getContext(), Story_Fragment.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();

                // get current date
                Calendar currentDate = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
                final String date = simpleDateFormat.format(currentDate.getTime());

                // get current time
                Calendar currentTime = Calendar.getInstance();
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
                final String time = simpleTimeFormat.format(currentTime.getTime());

                Toast.makeText(getContext(), "Uploading Story...", Toast.LENGTH_SHORT).show();

                // add story image to FirebaseStorage
                storage.getReference().child("Story").child(currentUser).child("Story Image.jpg").putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storage.getReference().child("Story").child(currentUser).child("Story Image.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                        // in the current users database, check for those IDs who are friend with current user
                                        // if someone is friend with current user, than in his database add current users story image URL
                                        firestore.collection(currentUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                                    if(snapshot.getString("Request Type").equals("friends")){

                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("Story URL", uri.toString());
                                                        map.put("Story upload time", date+" at "+time);
                                                        firestore.collection(snapshot.getId()).document(currentUser).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getContext(), "Story uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else {
                                                                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        storyFragmentAdapterClass.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        storyFragmentAdapterClass.stopListening();
    }


}
