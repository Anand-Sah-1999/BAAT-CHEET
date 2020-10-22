package com.example.baatcheet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChat_AdapterClass extends RecyclerView.Adapter<PrivateChat_AdapterClass.ViewHolder> {

    ArrayList<PrivateChat_GetterSetter> messagesList;
    String receiverProfilePicURL;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    Context context;
    GetClickedItemPosition getClickedItemPosition;

    PrivateChat_AdapterClass(Context context, String receiverProfilePicURL, ArrayList<PrivateChat_GetterSetter> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
        this.receiverProfilePicURL = receiverProfilePicURL;
    }

    // this method will prevent recycler view data to get messed up/to make its duplicate while scrolling. (1/2)
    @Override
    public long getItemId(int position) {
        return position;
    }

    // this method will prevent recycler view data to get messed up/to make its duplicate while scrolling. (2/2)
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface GetClickedItemPosition{
        void clickedImagePosition(int pos);
        void clickedPDFPosition(int pos);
        void clickedMP3Position(int pos);
        void deleteTextPosition(int optionPosition,String uniqueMessageKey);
    }

    public void setClickedItemPosition(GetClickedItemPosition getClickedItemPosition){
        this.getClickedItemPosition = getClickedItemPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_text_style, parent, false);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String senderUniqueID = auth.getCurrentUser().getUid();
        PrivateChat_GetterSetter item = messagesList.get(position);

        String fromUserID = item.getSenderID();
        String messageType = item.getMessageType();
        String messageBody = item.getMessage();
        String date = item.getCurrentDate();
        String time = item.getCurrentTime();


        holder.receiver_profile_pic.setVisibility(View.INVISIBLE);
        holder.receivedMessage.setVisibility(View.INVISIBLE);
        holder.sentMessage.setVisibility(View.INVISIBLE);
        holder.sentMessageDate.setVisibility(View.INVISIBLE);
        holder.sentMessageTime.setVisibility(View.INVISIBLE);
        holder.receivedMessageDate.setVisibility(View.INVISIBLE);
        holder.receivedMessageTime.setVisibility(View.INVISIBLE);

        // if the message is text
        if(messageType.equals("text")){

            if(fromUserID.equals(senderUniqueID)){

                holder.sentMessage.setVisibility(View.VISIBLE);
                holder.sentMessageDate.setVisibility(View.VISIBLE);
                holder.sentMessageTime.setVisibility(View.VISIBLE);
                holder.sentMessageDate.setText(date);
                holder.sentMessageTime.setText(time);
                holder.sentMessage.setText(messageBody);
            }
            else{
                holder.receiver_profile_pic.setVisibility(View.VISIBLE);
                holder.receivedMessage.setVisibility(View.VISIBLE);
                holder.receivedMessageDate.setVisibility(View.VISIBLE);
                holder.receivedMessageTime.setVisibility(View.VISIBLE);
                holder.receivedMessageDate.setText(date);
                holder.receivedMessageTime.setText(time);
                holder.receivedMessage.setText(messageBody);

                firebaseFirestore.collection("Users").document(fromUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String ImageUrl = documentSnapshot.getString("Image URL");
                        Picasso.with(context).load(ImageUrl).placeholder(R.drawable.default_dp).into(holder.receiver_profile_pic);
                    }
                });
            }
        }

        // if the message is NOT text and is image, pdf, mp3 etc...
        else {
            if(fromUserID.equals(senderUniqueID)) {

                holder.senderSentImage.setVisibility(View.VISIBLE);
                holder.sentMessageDate.setVisibility(View.VISIBLE);
                holder.sentMessageTime.setVisibility(View.VISIBLE);
                holder.sentMessageDate.setText(date);
                holder.sentMessageDate.setTextColor(Color.WHITE);
                holder.sentMessageTime.setText(time);
                holder.sentMessageTime.setTextColor(Color.WHITE);

                if(messageType.equals("jpg")) {
                    Picasso.with(context).load(messageBody).fit().into(holder.senderSentImage);
                }
                else if(messageType.equals("pdf")){
                    Picasso.with(context).load(R.drawable.pdf_vector_new).fit().into(holder.senderSentImage);
                }
                else if(messageType.equals("mp3")){
                    holder.senderSentImage.setVisibility(View.GONE);
                    holder.senderSentMP3.setVisibility(View.VISIBLE);
                }
            }
            else{
                holder.receiver_profile_pic.setVisibility(View.VISIBLE);
                holder.receiverSentImage.setVisibility(View.VISIBLE);
                holder.receivedMessageDate.setVisibility(View.VISIBLE);
                holder.receivedMessageDate.setText(date);
                holder.receivedMessageDate.setTextColor(Color.WHITE);
                holder.receivedMessageTime.setText(time);
                holder.receivedMessageTime.setTextColor(Color.WHITE);
                holder.receivedMessageTime.setVisibility(View.VISIBLE);

                if(messageType.equals("jpg")) {
                    Picasso.with(context).load(messageBody).fit().into(holder.receiverSentImage);
                }
                else if(messageType.equals("pdf")){
                    Picasso.with(context).load(R.drawable.pdf_vector_new).fit().into(holder.receiverSentImage);
                }
                else if(messageType.equals("mp3")){
                    holder.receiverSentImage.setVisibility(View.GONE);
                    holder.receiverSentMP3.setVisibility(View.VISIBLE);
                }

                firebaseFirestore.collection("Users").document(fromUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String ImageUrl = documentSnapshot.getString("Image URL");
                        Picasso.with(context).load(ImageUrl).placeholder(R.drawable.default_dp).into(holder.receiver_profile_pic);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView receiver_profile_pic;
        TextView sentMessage, receivedMessage, sentMessageDate, sentMessageTime, receivedMessageDate, receivedMessageTime;
        ImageView receiverSentImage, senderSentImage, senderSentMP3, receiverSentMP3;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            sentMessage = itemView.findViewById(R.id.sentMessage);
            sentMessageDate = itemView.findViewById(R.id.sentMessageDate);
            sentMessageTime = itemView.findViewById(R.id.sentMessageTime);
            senderSentImage = itemView.findViewById(R.id.senderSentImage);
            senderSentMP3 = itemView.findViewById(R.id.senderSentMP3);
            receiverSentMP3 = itemView.findViewById(R.id.receiverSentMP3);
            receiver_profile_pic = itemView.findViewById(R.id.receiver_profile_pic);
            receivedMessage = itemView.findViewById(R.id.receivedMessage);
            receiverSentImage = itemView.findViewById(R.id.receiverSentImage);
            receivedMessageDate = itemView.findViewById(R.id.receivedMessageDate);
            receivedMessageTime = itemView.findViewById(R.id.receivedMessageTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    PrivateChat_GetterSetter item = messagesList.get(position);

                    // open/download pdf on clicking on it
                    if(item.getMessageType().equals("pdf")){
                        getClickedItemPosition.clickedPDFPosition(position);
                    }
                    else if(item.getMessageType().equals("jpg")){
                        getClickedItemPosition.clickedImagePosition(position);
                    }
                    else if(item.getMessageType().equals("mp3")){
                        getClickedItemPosition.clickedMP3Position(position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int position = getAdapterPosition();
                    final PrivateChat_GetterSetter item = messagesList.get(position);
                    itemView.setBackgroundColor(Color.parseColor("#F3F6F6"));

                    CharSequence deleteOptions[];
                    if(item.getSenderID().equals(auth.getCurrentUser().getUid())) {
                        deleteOptions = new CharSequence[]{"Delete for me", "Cancel", "Delete for everyone"};
                    }
                    else{
                        deleteOptions = new CharSequence[]{"Delete for me", "Cancel"};
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Select Action").setItems(deleteOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    getClickedItemPosition.deleteTextPosition(which,item.getUniqueMessageKey());
                                    itemView.setBackgroundColor(0);
                                    break;
                                case 1:
                                    dialog.cancel();
                                    itemView.setBackgroundColor(0);
                                    break;
                                case 2:
                                    getClickedItemPosition.deleteTextPosition(which,item.getUniqueMessageKey());
                                    itemView.setBackgroundColor(0);
                                    break;
                            }
                        }
                    }).setCancelable(false).create().show();
                    return true;
                }
            });
        }
    }


}
