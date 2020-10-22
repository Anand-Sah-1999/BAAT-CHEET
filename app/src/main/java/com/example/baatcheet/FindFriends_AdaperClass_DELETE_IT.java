package com.example.baatcheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriends_AdaperClass_DELETE_IT extends RecyclerView.Adapter<FindFriends_AdaperClass_DELETE_IT.ViewHolder> {

    Context context;
    ArrayList<FindFriends_GetterSetter> allUserList;
    GetPositionForClickEvents getPositionForClickEvents;

    public FindFriends_AdaperClass_DELETE_IT(Context context, ArrayList<FindFriends_GetterSetter> allUserList) {
        this.context = context;
        this.allUserList = allUserList;
    }

    public interface GetPositionForClickEvents{
        void getPos(int pos,ArrayList<FindFriends_GetterSetter> allUserList);
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_card_view, parent, false);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_pic = itemView.findViewById(R.id.profile_pic);
            displayName = itemView.findViewById(R.id.displayName);
            status = itemView.findViewById(R.id.status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    getPositionForClickEvents.getPos(pos,allUserList);

                }
            });
        }
    }
}
