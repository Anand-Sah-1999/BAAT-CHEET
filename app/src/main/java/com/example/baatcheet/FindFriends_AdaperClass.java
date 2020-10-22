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
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriends_AdaperClass extends FirestoreRecyclerAdapter<FindFriends_GetterSetter, FindFriends_AdaperClass.ViewHolder> {

    Context context;
    GetPositionForClickEvents getPositionForClickEvents;

        public FindFriends_AdaperClass(Context context, @NonNull FirestoreRecyclerOptions<FindFriends_GetterSetter> options) {
        super(options);
        this.context = context;
    }

    public interface GetPositionForClickEvents{
        void getItemClickedPos(DocumentSnapshot documentSnapshot, int pos);
    }

    public void SetMethodsForClickEvents(GetPositionForClickEvents getPositionForClickEvents){
        this.getPositionForClickEvents = getPositionForClickEvents;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull FindFriends_GetterSetter model) {
        getSnapshots().getSnapshot(position).getReference().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Picasso.with(context).load(documentSnapshot.getString("Image URL")).placeholder(R.drawable.default_dp).into(holder.profile_pic);
                holder.displayName.setText(documentSnapshot.getString("Name"));
                holder.status.setText(documentSnapshot.getString("Status"));
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_card_view, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_pic;
        TextView displayName, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_pic = itemView.findViewById(R.id.profile_pic);
            displayName = itemView.findViewById(R.id.displayName);
            status = itemView.findViewById(R.id.status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    getPositionForClickEvents.getItemClickedPos(getSnapshots().getSnapshot(pos),pos);
                }
            });
        }
    }
}
