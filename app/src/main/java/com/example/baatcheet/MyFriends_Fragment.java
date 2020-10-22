package com.example.baatcheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class MyFriends_Fragment extends Fragment {

    RelativeLayout emptyMyFriendsLayout;
    Button findFriends;
    static RecyclerView myFriendsRecyclerView;
    static LinearLayoutManager linearLayoutManager;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    static String receiverUserID;
    static MyFriends_Fragment_AdapterClass myFriendsFragmentAdapterClass;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyFriends_Fragment() {
        // Required empty public constructor
    }

    public static MyFriends_Fragment newInstance(String param1, String param2) {
        MyFriends_Fragment fragment = new MyFriends_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    public static void searchForFriend(Context context, Query query) {
//
//        FirestoreRecyclerOptions<FindFriends_GetterSetter> options =
//                new FirestoreRecyclerOptions.Builder<FindFriends_GetterSetter>()
//                        .setQuery(query, FindFriends_GetterSetter.class)
//                        .build();
//
//        linearLayoutManager = new LinearLayoutManager(context);
//        myFriendsRecyclerView.setLayoutManager(linearLayoutManager);
//        myFriendsFragmentAdapterClass = new MyFriends_Fragment_AdapterClass(context, options);
//        myFriendsRecyclerView.setAdapter(myFriendsFragmentAdapterClass);
//
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    private void setViews() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        receiverUserID = auth.getCurrentUser().getUid();

        // rest of the views have been set inside "setUpRecyclerView()" method
    }

    private void setUpRecyclerView() {
        final Query query = firebaseFirestore.collection(receiverUserID).whereGreaterThan("Last Text Received Time", 0).orderBy("Last Text Received Time", Query.Direction.DESCENDING);
        // This query gets the names of all the users that have sent friend request to the current user.
        // Here, change the "friends" to "send" to get the list of all the friend request that you have sent to others.
        // the orderBy query will show the chats in according to "recent chat on top"

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots.size()==0){
                    //if there is no friends to show
                    emptyMyFriendsLayout.setVisibility(View.VISIBLE);
                    findFriends.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), Find_Friends.class));
                        }
                    });
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

        linearLayoutManager = new LinearLayoutManager(getActivity());
        myFriendsRecyclerView.setLayoutManager(linearLayoutManager);
        myFriendsFragmentAdapterClass = new MyFriends_Fragment_AdapterClass(getActivity(), options);
        myFriendsRecyclerView.setAdapter(myFriendsFragmentAdapterClass);

        myFriendsFragmentAdapterClass.SetMethodsForClickEvents(new MyFriends_Fragment_AdapterClass.GetPositionForClickEvents() {
            @Override
            public void getItemClickedPos(DocumentSnapshot documentSnapshot, int pos) {

                // when a card in recycler view is clicked, the info of that user is sent to the "Chat Activity" so that his name
                // and profile pic can be shown on the Action Bar
                final String receiverUserID = documentSnapshot.getString("User ID");
                final String receiverName = documentSnapshot.getString("Name");
                final String receiverProfilePicURL = documentSnapshot.getString("Image URL");

                Intent intent = new Intent(getActivity(), Chat_Activity.class);
                intent.putExtra("receiverUserID", receiverUserID);
                intent.putExtra("receiverName", receiverName);
                intent.putExtra("receiverProfilePicURL", receiverProfilePicURL);
                startActivity(intent);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_myfriends_, container, false);

        emptyMyFriendsLayout = view.findViewById(R.id.emptyMyFriendsLayout);
        findFriends = view.findViewById(R.id.findFriends);

        myFriendsRecyclerView = view.findViewById(R.id.myFriendsRecyclerView);
        myFriendsRecyclerView.setHasFixedSize(true);

        //this method prevents the cards in recycler view from blinking whenever the data in database is changed.
        myFriendsRecyclerView.getItemAnimator().setChangeDuration(0);
        setViews();
        setUpRecyclerView();

        return view;
    }

    // "myFriendsFragmentAdapterClass.startListening()" is VERY important, because without it, the recyclerView will not fetch data and display it
    @Override
    public void onStart() {
        super.onStart();

        myFriendsFragmentAdapterClass.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        myFriendsFragmentAdapterClass.stopListening();
    }
}
