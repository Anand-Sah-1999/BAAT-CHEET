package com.example.baatcheet;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.baatcheet.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TabbedActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
//        FloatingActionButton fab = findViewById(R.id.fab);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        setViews();
    }

    // it will be called when the user clicks the back button to close the app
    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.onPause();
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
        firebaseFirestore.collection("Users").document(currentUser).set(map, SetOptions.merge());

        // puts offline status of in the current user in receivers database, so that he can see if other user is inline or not
        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                    final String receiverUserID = snapshot.getString("User ID");
                    firebaseFirestore.collection(receiverUserID).document(currentUser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists() && !receiverUserID.equals(currentUser)) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("User is", "Offline");
                                firebaseFirestore.collection(receiverUserID).document(currentUser).set(map, SetOptions.merge());
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(auth.getCurrentUser()!=null) {
            // when user will open this app, "User is" field in database will be updated to "online" and it will be shown to other users
            HashMap<String, Object> map = new HashMap<>();
            map.put("User is", "Online");
            firebaseFirestore.collection("Users").document(currentUser).set(map, SetOptions.merge());

            // puts online status of in the current user in receivers database, so that he can see if other user is inline or not
            firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){

                        final String receiverUserID = snapshot.getString("User ID");
                        firebaseFirestore.collection(receiverUserID).document(currentUser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists() && !receiverUserID.equals(auth.getCurrentUser().getUid())) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("User is", "Online");
                                    firebaseFirestore.collection(receiverUserID).document(currentUser).set(map, SetOptions.merge());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.tabbed_activity_menu, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);
//        MenuItem menuItem = menu.findItem(R.id.app_bar_search2);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Query query;
//
//                if(newText.equals("")){
//                    query = firebaseFirestore.collection(currentUser).whereEqualTo("Request Type", "friends");
//                }
//                else{
//                    query = firebaseFirestore.collection(currentUser).whereEqualTo("Request Type", "friends")
//                            .orderBy("Request Type").startAt(newText).endAt(newText + "\uf8ff");
//                }
//                MyFriends_Fragment myFriendsFragment = new MyFriends_Fragment();
//                myFriendsFragment.searchForFriend(TabbedActivity.this, query);
//                return true;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemID = item.getItemId();
        switch(itemID){
            case R.id.signOut:
                auth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TabbedActivity.this, Login.class));
                break;

            case R.id.findFriends:
                startActivity(new Intent(TabbedActivity.this, Find_Friends.class));
                break;

            case R.id.settings:
                startActivity(new Intent(TabbedActivity.this, Settings.class));
                break;

            case R.id.friendRequests:
                startActivity(new Intent(TabbedActivity.this, Friend_Requests_Display.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setViews(){
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        if(auth.getCurrentUser()!=null) {
            currentUser = auth.getCurrentUser().getUid();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);


        // close all activities and close the app
        finishAffinity();
    }
}