package com.example.baatcheet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baatcheet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat_Activity extends AppCompatActivity {

    Calendar getDate, getTime;
    String receiverUserID, senderUserID, receiverName, receiverProfilePicURL, currentDate, currentTime, receiverOnlineStatus, receiverLastSeenDate, receiverLastSeenTime;
    CircleImageView profile_pic;
    TextView displayName, lastSeen;
    EditText messageText;
    ImageView back, showBlackBackground, showFullImage;
    FirebaseFirestore firebaseFirestore;
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    FirebaseAuth auth;
    ArrayList<PrivateChat_GetterSetter> messagesList;
    RecyclerView chatRecyclerView;
    LinearLayoutManager linearLayoutManager;
    PrivateChat_AdapterClass privateChatAdapterClass;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_);

        setActionBarViews();
        getIntentDataAndSetItToViews();

        // This button will add the message to firebase and will show it to both users
        FloatingActionButton sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessageMethod();
            }
        });
    }

    // onStart method is used to so that the data is fetched when this page is opened and NOT when app is opened
    @Override
    protected void onStart() {
        super.onStart();

        getArrayElements();

        if(auth.getCurrentUser()!=null) {
            // when user will open this app, "User is" field in database will be updated to "online" and it will be shown to other users
            HashMap<String, Object> map = new HashMap<>();
            map.put("User is", "Online");
            firebaseFirestore.collection("Users").document(senderUserID).set(map, SetOptions.merge());


            // puts online/offline status of in the current user in receivers database, so that he can see if other user is inline or not
            firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                        final String receiverUserID = snapshot.getString("User ID");
                        firebaseFirestore.collection(receiverUserID).document(senderUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists() && !receiverUserID.equals(auth.getCurrentUser().getUid())) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("User is", "Online");
                                    firebaseFirestore.collection(receiverUserID).document(senderUserID).set(map, SetOptions.merge());
                                }
                            }
                        });
                    }
                }
            });


            // when receiver opens the text sent by sender, update it as "Read" in receivers database. so that receiver can see
            // the senders name in un-highlight/bold color.
            HashMap<String, Object> map2 = new HashMap<>();
            map2.put("Read or Unread", "Read");
            firebaseFirestore.collection(senderUserID).document(receiverUserID).set(map2, SetOptions.merge());
        }
    }

    // it will be called when the user clicks the home button or the "current used apps" button to close the app
    @Override
    protected void onPause() {
        super.onPause();

        // when user will close this app, "User is" field in database will be updated to "offline" and it will be shown to other users
        // also it will show the last time, when he was online
        Calendar getDate = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String getLastSeenDate = simpleDateFormat.format(getDate.getTime());

        Calendar getTime = Calendar.getInstance();
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
        String getLastSeenTime = simpleTimeFormat.format(getTime.getTime());

        HashMap<String, Object> map = new HashMap<>();
        map.put("User is", "Offline");
        map.put("Last Seen Date", getLastSeenDate);
        map.put("Last Seen Time", getLastSeenTime);
        firebaseFirestore.collection("Users").document(senderUserID).set(map, SetOptions.merge());


        // puts online/offline status of in the current user in receivers database, so that he can see if other user is inline or not
        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                    final String receiverUserID = snapshot.getString("User ID");
                    firebaseFirestore.collection(receiverUserID).document(senderUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists() && !receiverUserID.equals(auth.getCurrentUser().getUid())) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("User is", "Offline");
                                firebaseFirestore.collection(receiverUserID).document(senderUserID).set(map, SetOptions.merge());
                            }
                        }
                    });
                }
            }
        });
    }

    private void getArrayElements() {
        messagesList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Here .addChildEventListener will update the messagesList and will show the messages to user in real time
        databaseReference.child("Private Chat").child(senderUserID).child(receiverUserID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        PrivateChat_GetterSetter item = snapshot.getValue(PrivateChat_GetterSetter.class);
                        messagesList.add(item);

                        chatRecyclerView = findViewById(R.id.chatRecyclerView);
                        chatRecyclerView.setHasFixedSize(true);
                        linearLayoutManager = new LinearLayoutManager(Chat_Activity.this);
                        linearLayoutManager.setStackFromEnd(true);
                        chatRecyclerView.setLayoutManager(linearLayoutManager);
                        privateChatAdapterClass = new PrivateChat_AdapterClass(Chat_Activity.this, receiverProfilePicURL, messagesList);
                        chatRecyclerView.setAdapter(privateChatAdapterClass);
                        privateChatAdapterClass.notifyDataSetChanged();

                        // this method will be called when a card in the recyclerView is clicked
                        handleItemClickEvents();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void handleItemClickEvents(){

        privateChatAdapterClass.setClickedItemPosition(new PrivateChat_AdapterClass.GetClickedItemPosition() {
            @Override
            public void clickedImagePosition(int pos) {
                showBlackBackground.setVisibility(View.VISIBLE);
                showFullImage.setVisibility(View.VISIBLE);

                PrivateChat_GetterSetter item = messagesList.get(pos);
                String imageURI = item.getMessage();
                Picasso.with(Chat_Activity.this).load(imageURI).placeholder(R.drawable.loading_icon).fit().into(showFullImage);
            }

            @Override
            public void clickedPDFPosition(int pos) {
                PrivateChat_GetterSetter item = messagesList.get(pos);
                String PDFLink = item.getMessage();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PDFLink));
                startActivity(intent);
            }

            @Override
            public void clickedMP3Position(int pos) {
                PrivateChat_GetterSetter item = messagesList.get(pos);
                String mp3Link = item.getMessage();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mp3Link));
                startActivity(intent);
            }

            @Override
            public void deleteTextPosition(int optionPosition, final String uniqueMessageKey) {
                // 0 is for "delete for me" option
                if(optionPosition==0){
                    databaseReference.child("Private Chat").child(senderUserID).child(receiverUserID).child(uniqueMessageKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Chat_Activity.this, "Message Deleted.\nReopen the chat activity to apply changes", Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(Chat_Activity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // else means 2, which is for "delete for everyone" option
                else {
                    databaseReference.child("Private Chat").child(senderUserID).child(receiverUserID).child(uniqueMessageKey).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    databaseReference.child("Private Chat").child(receiverUserID).child(senderUserID).child(uniqueMessageKey).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(Chat_Activity.this, "Message Deleted.\nReopen the chat activity to apply changes", Toast.LENGTH_LONG).show();
                                                    }
                                                    else{
                                                        Toast.makeText(Chat_Activity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                }
            }
        });
    }

    private void setActionBarViews() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Getting the color to set in Action Bar
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#3597D1"));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(colorDrawable);

        // Set a layout in the Action Bar
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(view);

        profile_pic = view.findViewById(R.id.profile_picX);
        displayName = findViewById(R.id.displayNameX);
        lastSeen = findViewById(R.id.lastSeenX);
        back = findViewById(R.id.back);
        messageText = findViewById(R.id.messageText);
        showBlackBackground = findViewById(R.id.showBlackBackground);
        showFullImage = findViewById(R.id.showFullImage);

        auth = FirebaseAuth.getInstance();
        senderUserID = auth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(Chat_Activity.this);
    }

    private void getIntentDataAndSetItToViews() {

        // Get the info of the user on the other side of the chat, and show it on Action Bar
        receiverUserID = getIntent().getStringExtra("receiverUserID");
        receiverName = getIntent().getStringExtra("receiverName");
        receiverProfilePicURL = getIntent().getStringExtra("receiverProfilePicURL");

        Picasso.with(this).load(receiverProfilePicURL).placeholder(R.drawable.default_dp).into(profile_pic);
        displayName.setText(receiverName);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(receiverUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    receiverOnlineStatus = documentSnapshot.getString("User is");
                    receiverLastSeenDate = documentSnapshot.getString("Last Seen Date");
                    receiverLastSeenTime = documentSnapshot.getString("Last Seen Time");

                    if(receiverOnlineStatus.equals("Online")) {
                        lastSeen.setText("Online");
                    }
                    else{
                        lastSeen.setText("Last Seen on " + receiverLastSeenDate + " at " + receiverLastSeenTime);
                    }
                }
            }
        });

    }

    public void SendMessageMethod(){

        final String message = messageText.getText().toString();
        if(message.isEmpty()){

            Toast.makeText(this, "Text field empty..!!", Toast.LENGTH_SHORT).show();
        }
        else{

            // get the date on which he message has been sent
            getDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(getDate.getTime());

            // get the time on which he message has been sent
            getTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(getTime.getTime());

            String messageSenderRef = "Private Chat/" + senderUserID + "/" + receiverUserID;
            String messageReceiverRef = "Private Chat/" + receiverUserID + "/" + senderUserID;

            //Get unique key of for each text sent
            String uniqueMessageKey = databaseReference.child("Private Chat").child(senderUserID).child(receiverUserID).push().getKey();

            // upload text data to both senders and receivers database
            Map<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("senderID", senderUserID);
            messageInfo.put("messageType", "text");
            messageInfo.put("message", message);
            messageInfo.put("currentDate", currentDate);
            messageInfo.put("currentTime", currentTime);
            messageInfo.put("uniqueMessageKey", uniqueMessageKey);

            Map<String, Object> messagePath = new HashMap<>();
            messagePath.put(messageSenderRef + "/" + uniqueMessageKey, messageInfo);
            messagePath.put(messageReceiverRef + "/" + uniqueMessageKey, messageInfo);

            databaseReference.updateChildren(messagePath);
            messageText.setText("");

            // Show the last message to the sender and the receiver, in his recyclerView chat card view
            // And will also add the last time when the sender sends a text
            HashMap<String, Object> map = new HashMap<>();
            map.put("Status", message);
            map.put("Last Text Received Time", System.currentTimeMillis());
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(senderUserID).document(receiverUserID).set(map, SetOptions.merge());
            firebaseFirestore.collection(receiverUserID).document(senderUserID).set(map, SetOptions.merge());

            // when sender sends a text to receiver, update it as "Unread" in receivers database. so that receiver can see
            // the senders name in highlight/bold color.
            HashMap<String, Object> map2 = new HashMap<>();
            map2.put("Read or Unread", "Unread");
            firebaseFirestore.collection(receiverUserID).document(senderUserID).set(map2, SetOptions.merge());
        }
    }

    public void sendImage(View view){
        Intent openGalleryIntent = new Intent();
        openGalleryIntent.setType("image/*");
        openGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(openGalleryIntent, 1);
    }

    public void sendDocument(View view){
        RelativeLayout relativeLayoutPDF, relativeLayoutMP3;

        // inflate the xml and attach it in the alert box
        View chooseDocument = LayoutInflater.from(this).inflate(R.layout.choose_document_to_send, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setView(chooseDocument);

        // AlertDialog.Builder object will not cancel the dialogue box when .dismiss() method is called inside the view's onClickListener
        // thus to cancel it on view click, we have to create an object of AlertDialogue
        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        relativeLayoutPDF = chooseDocument.findViewById(R.id.relativeLayoutPDF);
        relativeLayoutMP3 = chooseDocument.findViewById(R.id.relativeLayoutMP3);

        relativeLayoutPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent();
                openGalleryIntent.setType("application/pdf");
                openGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(openGalleryIntent, 1);
                dialog.dismiss();
            }
        });

        relativeLayoutMP3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent();
                openGalleryIntent.setType("audio/*");
                openGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(openGalleryIntent, 1);
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK && data != null && data.getData() != null){

            Uri selectedDocumentURI = data.getData();

            // get the extension of a file. Ex: jpg, pdf, mp3 etc
            final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(selectedDocumentURI));

            // get the date on which he message has been sent
            getDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(getDate.getTime());

            // get the time on which he message has been sent
            getTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(getTime.getTime());

            final String messageSenderRef = "Private Chat/" + senderUserID + "/" + receiverUserID;
            final String messageReceiverRef = "Private Chat/" + receiverUserID + "/" + senderUserID;

            //Get unique key of for each text sent
            final String uniqueMessageKey = databaseReference.child("Private Chat").child(senderUserID).child(receiverUserID).push().getKey();

            // Add image to Storage first with its extension
            storage.getReference().child("PrivateChat_Images").child(uniqueMessageKey+"."+extension).putFile(selectedDocumentURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();
                    Toast.makeText(Chat_Activity.this, "File Sent Successfully", Toast.LENGTH_SHORT).show();
                    storage.getReference().child("PrivateChat_Images").child(uniqueMessageKey+"."+extension).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadedURI = uri.toString();

//                             upload text data to both senders and receivers database
                            Map<String, Object> messageInfo = new HashMap<>();
                            messageInfo.put("senderID", senderUserID);
                            messageInfo.put("messageType", extension);
                            messageInfo.put("message", downloadedURI);
                            messageInfo.put("currentDate", currentDate);
                            messageInfo.put("currentTime", currentTime);
                            messageInfo.put("uniqueMessageKey", uniqueMessageKey);

                            Map<String, Object> messagePath = new HashMap<>();
                            messagePath.put(messageSenderRef + "/" + uniqueMessageKey, messageInfo);
                            messagePath.put(messageReceiverRef + "/" + uniqueMessageKey, messageInfo);

                            databaseReference.updateChildren(messagePath);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    int uploadedBytes = Math.round((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    progressDialog.setTitle("Sending file, please wait...   ");
                    progressDialog.setMessage(uploadedBytes + " % completed");
                    progressDialog.show();
                }
            });

//             Show the last message to the sender and the receiver, in his recyclerView chat card view
            HashMap<String, Object> map = new HashMap<>();
            if(extension.equals("jpg")) {
                map.put("Status", "** Image File **");
            }
            else if(extension.equals("pdf")){
                map.put("Status", "** PDF File **");
            }
            else if(extension.equals("mp3")){
                map.put("Status", "** Audio File **");
            }
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(senderUserID).document(receiverUserID).set(map, SetOptions.merge());
            firebaseFirestore.collection(receiverUserID).document(senderUserID).set(map, SetOptions.merge());

            // when sender sends a text to receiver, update it as "Unread" in receivers database. so that receiver can see
            // the senders name in highlight/bold color.
            HashMap<String, Object> map2 = new HashMap<>();
            map2.put("Read or Unread", "Unread");
            firebaseFirestore.collection(receiverUserID).document(senderUserID).set(map2, SetOptions.merge());
        }
    }

    // this is the back arrow on the action bar
    public void goBack(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        // if full screen image is opened, then close it
        if(showBlackBackground.getVisibility() == View.VISIBLE) {
            showBlackBackground.setVisibility(View.GONE);
            showFullImage.setVisibility(View.GONE);
        }
        // else go back
        else {
            startActivity(new Intent(Chat_Activity.this, TabbedActivity.class));
        }
    }
}
