package com.example.baatcheet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowUserProfile extends AppCompatActivity {

    CircleImageView profile_image3;
    TextView name, status;
    String uniqueUserID;        // This is the ID of the person you want to send request to. It is NOT your own userID
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String friend_req_status, friendship_status;
    String senderUserID, receiverUserID;
    Button sendFriendRequest, deleteFriendRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_profile);

        setViews();
        set_User_Info_in_Views_From_Firebase();
    }

    private void setViews() {
        profile_image3 = findViewById(R.id.profile_image3);
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        sendFriendRequest = findViewById(R.id.sendFriendRequest);
        deleteFriendRequest = findViewById(R.id.deleteFriendRequest);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        friend_req_status = "new";
        friendship_status = "not friends";

        uniqueUserID = getIntent().getStringExtra("unique userID");
        senderUserID = auth.getCurrentUser().getUid();
        receiverUserID = uniqueUserID;                  // This is the ID of the person you want to send request to. It is NOT your own userID
    }

    private void set_User_Info_in_Views_From_Firebase() {

        updateSendRequestInfo();            // This method manages what will happen on friend request send/ deleted
        updateFriendsInfo();                // This method manages what will happen on friend request accepted

        firebaseFirestore.collection("Users").document(uniqueUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Picasso.with(ShowUserProfile.this).load(documentSnapshot.getString("Image URL")).placeholder(R.drawable.default_dp).into(profile_image3);
                name.setText(documentSnapshot.getString("Name"));
                status.setText(documentSnapshot.getString("Status"));
            }
        });

        if(senderUserID.equals(receiverUserID)){
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void updateSendRequestInfo() {
        firebaseFirestore.collection("Friend Requests").document(senderUserID).collection(receiverUserID)
                .document("Request Status").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String sentStatus = documentSnapshot.getString("Status");
                    if (sentStatus.equals("send")) {
                        friend_req_status = "send";
                        sendFriendRequest.setText("Cancel Friend Request");
                        sendFriendRequest.setBackgroundColor(0xFFFF4444);
                    }
                    else if(sentStatus.equals("received")){
                        friend_req_status = "received";
                        deleteFriendRequest.setVisibility(View.VISIBLE);
                        sendFriendRequest.setText("Accept Friend Request");
                        sendFriendRequest.setBackgroundResource(R.color.colorAcceptFriendRequest);
                    }
                    else {
                        friend_req_status = "new";
                        sendFriendRequest.setText("Send Friend Request");
                        sendFriendRequest.setBackgroundResource(R.color.colorSendButton);
                    }
                }
            }
        });
    }

    private void updateFriendsInfo() {

        firebaseFirestore.collection("Friends").document(senderUserID).collection(receiverUserID)
                .document("Friendship Status").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String friendsOrNot = documentSnapshot.getString("Status");
                    if(friendsOrNot.equals("friends")){
                        friendship_status = "friends";
                        friend_req_status = "default";
                        sendFriendRequest.setText("Unfriend");
                        deleteFriendRequest.setVisibility(View.INVISIBLE);
                        sendFriendRequest.setBackgroundResource(R.color.colorUnfriend);
                    }
                }
            }
        });
    }

    public void SendAndCancelFriendRequest(View view){

        if(friend_req_status.equals("new")) {                   // This code will send friend request
            sendFriendRequestToUser();
        }
        else if(friend_req_status.equals("send")){          // This code will accept friend request
            cancelFriendRequestOfUser();
        }
        else {                                                  //This code will delete friend request
            acceptFriendRequestOfUser();
        }
    }

    private void sendFriendRequestToUser() {
        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("Status", "send");

        firebaseFirestore.collection("Friend Requests").document(senderUserID).collection(receiverUserID)
                .document("Request Status").set(map1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                HashMap<String, Object> map2 = new HashMap<>();
                map2.put("Status", "received");
                firebaseFirestore.collection("Friend Requests").document(receiverUserID).collection(senderUserID)
                        .document("Request Status").set(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendFriendRequest.setText("Cancel Friend Request");
                        sendFriendRequest.setBackgroundColor(0xFFFF4444);
                        friend_req_status = "send";
                        Toast.makeText(ShowUserProfile.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();

                        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for(QueryDocumentSnapshot snapshot2 : queryDocumentSnapshots) {
                                    final String currentItemID = snapshot2.getString("User ID");
                                    if (receiverUserID.equals(currentItemID)) {

                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("Image URL", snapshot2.getString("Image URL"));
                                        map.put("Name", snapshot2.getString("Name"));
                                        map.put("Request Type", "send");
                                        map.put("Status", snapshot2.getString("Status"));
                                        map.put("User ID", snapshot2.getString("User ID"));
                                        map.put("User is", "Offline");
                                        map.put("Read or Unread", "Read");
                                        map.put("Story URL", "Null");
                                        map.put("Story upload time", "Null");
                                        map.put("Last Text Received Time", 0);

                                        firebaseFirestore.collection(auth.getCurrentUser().getUid()).document(receiverUserID).set(map);
                                    }

                                    if (senderUserID.equals(currentItemID)) {

                                        HashMap<String, Object> map2 = new HashMap<>();
                                        map2.put("Image URL", snapshot2.getString("Image URL"));
                                        map2.put("Name", snapshot2.getString("Name"));
                                        map2.put("Request Type", "received");
                                        map2.put("Status", snapshot2.getString("Status"));
                                        map2.put("User ID", snapshot2.getString("User ID"));
                                        map2.put("User is", "Offline");
                                        map2.put("Read or Unread", "Read");
                                        map2.put("Story URL", "Null");
                                        map2.put("Story upload time", "Null");
                                        map2.put("Last Text Received Time", 0);

                                        firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).set(map2);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void cancelFriendRequestOfUser() {

        firebaseFirestore.collection("Friend Requests").document(senderUserID).collection(receiverUserID)
                .document("Request Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseFirestore.collection("Friend Requests").document(receiverUserID).collection(senderUserID)
                        .document("Request Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendFriendRequest.setText("Send Friend Request");
                        sendFriendRequest.setBackgroundColor(0x3597D1);
                        sendFriendRequest.setBackgroundResource(R.color.colorSendButton);
                        friend_req_status = "new";
                        Toast.makeText(ShowUserProfile.this, "Friend Request Canceled", Toast.LENGTH_SHORT).show();

                        firebaseFirestore.collection(auth.getCurrentUser().getUid()).document(receiverUserID).delete();
                        firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).delete();
                    }
                });
            }
        });
    }

    private void acceptFriendRequestOfUser() {

        if(friendship_status.equals("not friends")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("Status", "friends");
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
                                            sendFriendRequest.setText("Unfriend");
                                            sendFriendRequest.setBackgroundResource(R.color.colorUnfriend);
                                            deleteFriendRequest.setVisibility(View.INVISIBLE);
                                            friendship_status = "friends";
                                            Toast.makeText(ShowUserProfile.this, "Friend Request Accepted", Toast.LENGTH_SHORT).show();


                                            firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    for(QueryDocumentSnapshot snapshot2 : queryDocumentSnapshots) {
                                                        final String currentItemID = snapshot2.getString("User ID");
                                                        if (receiverUserID.equals(currentItemID)) {

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
                                                            firebaseFirestore.collection(auth.getCurrentUser().getUid()).document(receiverUserID).set(map);
                                                        }

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
                                                            firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).set(map);
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
        else{
            firebaseFirestore.collection("Friends").document(senderUserID).collection(receiverUserID)
                    .document("Friendship Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    firebaseFirestore.collection("Friends").document(receiverUserID).collection(senderUserID)
                            .document("Friendship Status").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sendFriendRequest.setText("Send Friend Request");
                            sendFriendRequest.setBackgroundResource(R.color.colorSendButton);
                            deleteFriendRequest.setVisibility(View.INVISIBLE);
                            friend_req_status = "new";
                            Toast.makeText(ShowUserProfile.this, "Unfriended", Toast.LENGTH_SHORT).show();

                            firebaseFirestore.collection(auth.getCurrentUser().getUid()).document(receiverUserID).delete();
                            firebaseFirestore.collection(receiverUserID).document(auth.getCurrentUser().getUid()).delete();
                        }
                    });
                }
            });
        }
    }

    public void DeleteFriendRequest(View view){

        cancelFriendRequestOfUser();
        deleteFriendRequest.setVisibility(View.INVISIBLE);

    }
}
