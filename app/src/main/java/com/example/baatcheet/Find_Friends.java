package com.example.baatcheet;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;


public class Find_Friends extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    FindFriends_AdaperClass adaperClass;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find__friends);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setQueryForRecyclerViewItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.find_friends_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        android.widget.SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseFirestore = FirebaseFirestore.getInstance();
                Query query;
                // if search field is empty, show all users name
                if(newText.equals("")) {
                    query = firebaseFirestore.collection("Users").orderBy("Name");
                }
                // show users whose name starts with the "newText"
                else{
                    query = firebaseFirestore.collection("Users")
                            .orderBy("Name").startAt(newText).endAt(newText + "\uf8ff");

                }

                setUpRecyclerViewFromQuery(query);
                adaperClass.startListening();
                return true;
            }
        });
        return true;
    }

    private void setQueryForRecyclerViewItems() {

        // This query will help to show all user names in alphabetically ascending order
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query query = firebaseFirestore.collection("Users").orderBy("Name");

        setUpRecyclerViewFromQuery(query);

    }

    public void setUpRecyclerViewFromQuery(Query query){

        // "options" contains all the data of the users,who are short-listed by the query
        // this "options" are like the array list, which is sent to the adapter class
        FirestoreRecyclerOptions<FindFriends_GetterSetter> options =
                new FirestoreRecyclerOptions.Builder<FindFriends_GetterSetter>()
                        .setQuery(query, FindFriends_GetterSetter.class).build();


        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adaperClass = new FindFriends_AdaperClass(this, options);
        recyclerView.setAdapter(adaperClass);

        // getting the unique ID of the user on the other side, and showing his data in another activity
        adaperClass.SetMethodsForClickEvents(new FindFriends_AdaperClass.GetPositionForClickEvents() {
            @Override
            public void getItemClickedPos(DocumentSnapshot documentSnapshot, int pos) {

                String uniqueUserID = documentSnapshot.getString("User ID");
                Intent intent = new Intent(Find_Friends.this, ShowUserProfile.class);
                intent.putExtra("unique userID", uniqueUserID);
                startActivity(intent);
            }
        });
    }

    // "adaperClass.startListening()" is VERY important, because without it, the recyclerView will not fetch data and display it
    @Override
    protected void onStart() {
        super.onStart();

        adaperClass.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        adaperClass.stopListening();
    }
}

