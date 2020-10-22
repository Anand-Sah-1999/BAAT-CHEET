package com.example.baatcheet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;


public class Friend_Requests_Display extends AppCompatActivity {

    RelativeLayout emptyMyFriendsLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    static String receiverUserID;
    FriendRequest_AdapterClass findFriendsAdapterClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend__requests__display);

        setViews();
        setUpRecyclerView();
    }


    private void setViews() {
        emptyMyFriendsLayout = findViewById(R.id.emptyMyFriendsLayout);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        receiverUserID = auth.getCurrentUser().getUid();

        // rest of the views have been set inside "setUpRecyclerView()" method
    }


    private void setUpRecyclerView() {

        // This query gets the names of all the users that have sent friend request to the current user.
        // Here, change the "received" to "send" to get the list of all the friend request that you have sent to others.
        Query query = firebaseFirestore.collection(receiverUserID).whereEqualTo("Request Type", "received");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots.size()==0){
                    //if there is no friends to show
                    emptyMyFriendsLayout.setVisibility(View.VISIBLE);
                }
                else{
                    emptyMyFriendsLayout.setVisibility(View.GONE);
                }
            }
        });

        FirestoreRecyclerOptions<FindFriends_GetterSetter> options =
                new FirestoreRecyclerOptions.Builder<FindFriends_GetterSetter>()
                        .setQuery(query, FindFriends_GetterSetter.class)
                        .build();


        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        findFriendsAdapterClass = new FriendRequest_AdapterClass(this,options);
        recyclerView.setAdapter(findFriendsAdapterClass);

        findFriendsAdapterClass.SetMethodsForClickEvents(new FriendRequest_AdapterClass.GetPositionForClickEvents() {

            @Override
            public void getAcceptButtonPos(DocumentSnapshot documentSnapshot, int pos) {

                final String senderUserID = documentSnapshot.getString("User ID");

                // When the "Accept friend req" button is clicked, the data of the friend req. sender is deleted from the "Friend Req" database and is added in "Friends" database
                HashMap<String, Object> map = new HashMap<>();
                map.put("Status", "friends");
                assert senderUserID != null;
                firebaseFirestore.collection("Friends").document(senderUserID).collection(receiverUserID)
                        .document("Friendship Status").set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, Object> map2 = new HashMap<>();
                        map2.put("Status", "friends");
                        firebaseFirestore.collection("Friends").document(receiverUserID).collection(senderUserID)
                                .document("Friendship Status").set(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Friend Requests").document(senderUserID).collection(receiverUserID)
                                        .document("Request Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Friend Requests").document(receiverUserID).collection(senderUserID)
                                                .document("Request Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(Friend_Requests_Display.this, "Friend Request Accepted", Toast.LENGTH_SHORT).show();

                                                // After being "friends", the data of both users are added in others database, so that his name can be shown the users "MyFriends" recyclerView activity
                                                firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for(QueryDocumentSnapshot snapshot2 : queryDocumentSnapshots) {
                                                            final String currentItemID = snapshot2.getString("User ID");

                                                            // if the "currentItemID" equals the id of the person who sent the friend req to me, then in my database, his info will be added
                                                            if (senderUserID.equals(currentItemID)) {

                                                                HashMap<String, Object> map = new HashMap<>();
                                                                map.put("Image URL", snapshot2.getString("Image URL"));
                                                                map.put("Name", snapshot2.getString("Name"));
                                                                map.put("Request Type", "friends");
                                                                map.put("Status", snapshot2.getString("Status"));
                                                                map.put("User ID", snapshot2.getString("User ID"));
                                                                map.put("User is", "Offline");
                                                                map.put("Read or Unread", "Read");
                                                                map.put("Last Text Received Time", 1);
                                                                map.put("Story URL", "Null");
                                                                map.put("Story upload time", "Null");
                                                                firebaseFirestore.collection(auth.getCurrentUser().getUid()).document(senderUserID).set(map);
                                                            }

                                                            // if the "currentItemID" equals my userID, then in his database, my info will be added
                                                            if (auth.getCurrentUser().getUid().equals(currentItemID)) {

                                                                HashMap<String, Object> map = new HashMap<>();
                                                                map.put("Image URL", snapshot2.getString("Image URL"));
                                                                map.put("Name", snapshot2.getString("Name"));
                                                                map.put("Request Type", "friends");
                                                                map.put("Status", snapshot2.getString("Status"));
                                                                map.put("User ID", snapshot2.getString("User ID"));
                                                                map.put("User is", "Offline");
                                                                map.put("Read or Unread", "Read");
                                                                map.put("Last Text Received Time", 1);
                                                                map.put("Story URL", "Null");
                                                                map.put("Story upload time", "Null");
                                                                firebaseFirestore.collection(senderUserID).document(auth.getCurrentUser().getUid()).set(map);
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void getDeleteButtonPos(DocumentSnapshot documentSnapshot, int pos) {

                final String senderUserID = documentSnapshot.getString("User ID");

                // when "delete friend req" button is clicked, the request senders info is deleted from the "Friend Req" database
                firebaseFirestore.collection("Friend Requests").document(senderUserID).collection(receiverUserID)
                        .document("Request Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseFirestore.collection("Friend Requests").document(receiverUserID).collection(senderUserID)
                                .document("Request Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Friend_Requests_Display.this, "Friend Request Canceled", Toast.LENGTH_SHORT).show();

                                // also, his data is deleted from the both the users database
                                firebaseFirestore.collection(auth.getCurrentUser().getUid()).document(senderUserID).delete();
                                firebaseFirestore.collection(senderUserID).document(auth.getCurrentUser().getUid()).delete();

                            }
                        });
                    }
                });
            }
        });
    }

    // "findFriendsAdapterClass.startListening()" is VERY important, because without it, the recyclerView will not fetch data and display it
    @Override
    protected void onStart() {
        super.onStart();

        findFriendsAdapterClass.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        findFriendsAdapterClass.stopListening();
    }
}
