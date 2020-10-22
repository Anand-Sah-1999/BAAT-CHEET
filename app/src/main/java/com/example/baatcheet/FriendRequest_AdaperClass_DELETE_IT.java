package com.example.baatcheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequest_AdaperClass_DELETE_IT extends RecyclerView.Adapter<FriendRequest_AdaperClass_DELETE_IT.ViewHolder> {

    Context context;
    ArrayList<FindFriends_GetterSetter> allUserList;
    GetPositionForClickEvents getPositionForClickEvents;

    public FriendRequest_AdaperClass_DELETE_IT(Context context, ArrayList<FindFriends_GetterSetter> allUserList) {
        this.context = context;
        this.allUserList = allUserList;
    }

    public interface GetPositionForClickEvents{
        void getConfirmButtonPos(int pos, ArrayList<FindFriends_GetterSetter> allUserList);
        void getDeleteButtonPos(int pos, ArrayList<FindFriends_GetterSetter> allUserList);
    }

    public void SetMethodsForClickEvents(GetPositionForClickEvents getPositionForClickEvents){
        this.getPositionForClickEvents = getPositionForClickEvents;
    }

    public void searchList(ArrayList<FindFriends_GetterSetter> searchedUserList) {
        allUserList = searchedUserList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FindFriends_GetterSetter item = allUserList.get(position);

        Picasso.with(context).load(item.getImageURL()).placeholder(R.drawable.default_dp).into(holder.profile_pic);
        holder.displayName.setText(item.getDisplayName());
        holder.status.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        return allUserList.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        CircleImageView profile_pic;
        TextView displayName, status;
        Button confirm, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_pic = itemView.findViewById(R.id.profile_pic);
            displayName = itemView.findViewById(R.id.displayName);
            status = itemView.findViewById(R.id.status);
            confirm = itemView.findViewById(R.id.confirm);
            delete = itemView.findViewById(R.id.delete);

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    getPositionForClickEvents.getConfirmButtonPos(pos,allUserList);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    getPositionForClickEvents.getDeleteButtonPos(pos,allUserList);
                }
            });
        }
    }
}
