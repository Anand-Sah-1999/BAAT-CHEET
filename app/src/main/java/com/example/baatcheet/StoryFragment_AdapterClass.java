package com.example.baatcheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryFragment_AdapterClass extends FirestoreRecyclerAdapter<FindFriends_GetterSetter, StoryFragment_AdapterClass.ViewHolder> {

    Context context;
    GetClickedCardPosition getClickedCardPosition;

    public interface GetClickedCardPosition{
        void getPosition(DocumentSnapshot documentSnapshot);
    }

    public void setClickedCardPosition(GetClickedCardPosition getClickedCardPosition){
        this.getClickedCardPosition = getClickedCardPosition;
    }

    public StoryFragment_AdapterClass(Context context,@NonNull FirestoreRecyclerOptions<FindFriends_GetterSetter> options) {
        super(options);

        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull FindFriends_GetterSetter model) {
        getSnapshots().getSnapshot(position).getReference().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.getString("Story URL").equals("Null")){
                    Picasso.with(context).load(documentSnapshot.getString("Story URL")).placeholder(R.drawable.default_dp).fit().into(holder.story_pic);
                    holder.displayName.setText(documentSnapshot.getString("Name"));
                    holder.dateAndTime.setText(documentSnapshot.getString("Story upload time"));
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_card_view, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView story_pic;
        TextView displayName, dateAndTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            story_pic = itemView.findViewById(R.id.profile_pic);
            displayName = itemView.findViewById(R.id.displayName);
            dateAndTime = itemView.findViewById(R.id.status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
                    getClickedCardPosition.getPosition(documentSnapshot);
                }
            });
        }
    }
}
