package com.yogeshkulkarni.firebasetictactoe;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class ChoosePlayerActivity extends AppCompatActivity {
    private static final String TAG = ChoosePlayerActivity.class.getSimpleName();
    EditText etInviteEMail;
    Button inviteButton;
    private ProgressBar progressBar;

    User currentOpponent;
    User loggedInUser;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_player);
        etInviteEMail =  findViewById(R.id.etInviteEmal);
        progressBar =  findViewById(R.id.progressBar);
        inviteButton =  findViewById(R.id.buInvite);

        ArrayList<User> arrayOfUsers = new ArrayList<>();
        final UserAdapter adapter = new UserAdapter(this, arrayOfUsers);

        final ListView listView =  findViewById(R.id.myListView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentOpponent = (User)listView.getAdapter().getItem(position);
                etInviteEMail.setText(currentOpponent.email);
            }
        });
        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");

        final FirebaseUser currentUserLoggedIn = FirebaseAuth.getInstance().getCurrentUser();

        Query allUsers = mFirebaseDatabase.orderByChild("name");

        allUsers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);

                Log.v(TAG, "User data:  " + user.myID + ", " + user.name + ", " + user.email);

                // get the current user from the database
                if (currentUserLoggedIn.getEmail().equals(user.email)) {
                    loggedInUser = user;

                    // go  to the game screen if a game is in progress
                    if (user.currentlyPlaying) {
                        startActivity(new Intent(ChoosePlayerActivity.this, GameActivity.class));
                        finish();
                    }
                }

                // if the other user is not currently playing and they do not have a
                // current request, then they are a valid opponent to choose
                else if (!user.currentlyPlaying && user.opponentID.isEmpty())
                {
                    adapter.add(user);
                }
            }



            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                User user = dataSnapshot.getValue(User.class);
                String userID = dataSnapshot.getKey();

                // if the other user is not currently playing and they do not have a
                // current request, then they are a valid opponent to choose
                // get the current user from the database
                if (!currentUserLoggedIn.getEmail().equals(user.email)) {
                    if (user.currentlyPlaying || !user.opponentID.isEmpty()) {
                        adapter.remove(user);
                    }
                } else {

                    // update your object
                    loggedInUser = user;

                    if (user.request == true) {
                        showAcceptOrDenyInviteDialog();
                        user.request = false;
                        mFirebaseDatabase.child(userID).setValue(user);
                    } else if (user.accepted.equals("true")) {
                        // set values back to initial state and show button
                        progressBar.setVisibility(View.GONE);
                        inviteButton.setEnabled(true);

                        mFirebaseDatabase.child(loggedInUser.myID).child("accepted").setValue("none");

                        // show dialog and go to game screen
                        showAcceptOrDenyStatusDialog(true);
                    } else if (user.accepted.equals("false")) {

                        // set values back to initial state and show button
                        progressBar.setVisibility(View.GONE);
                        mFirebaseDatabase.child(loggedInUser.myID).child("opponentID").setValue("");
                        mFirebaseDatabase.child(loggedInUser.myID).child("opponentEmail").setValue("");
                        mFirebaseDatabase.child(loggedInUser.myID).child("accepted").setValue("none");

                        // show dialog
                        showAcceptOrDenyStatusDialog(false);
                        inviteButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                // get the current user from the database
                if (!currentUserLoggedIn.getEmail().equals(user.email))
                    adapter.remove(user);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        });

    }
    private void showAcceptOrDenyStatusDialog(final boolean status)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Game Invite Status...");

        // Setting Dialog Message
        if (status)
            alertDialog.setMessage("Your game with " + loggedInUser.opponentEmail + " has been accepted");
        else
            alertDialog.setMessage("Your game with " + loggedInUser.opponentEmail + " has been denied");


        // Setting Positive "Yes" Btn
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // navigate to game screen
                        if (status) {
                            startActivity(new Intent(ChoosePlayerActivity.this, GameActivity.class));
                        }
                    }
                });

        // Showing Alert Dialog
        alertDialog.show();
    }

    private void showAcceptOrDenyInviteDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Accept Game Invite...");

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to play tic tac toe against " + loggedInUser.opponentEmail + "?");

        // Setting Positive "Yes" Btn
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // create game and go there
                        Game game = new Game(loggedInUser.opponentID);

                        mFirebaseDatabase.child(loggedInUser.opponentID).child("myGame").setValue(game);
                        mFirebaseDatabase.child(loggedInUser.myID).child("myGame").setValue(game);

                        // set game status for both players (currently playing)
                        mFirebaseDatabase.child(loggedInUser.opponentID).child("currentlyPlaying").setValue(true);
                        mFirebaseDatabase.child(loggedInUser.myID).child("currentlyPlaying").setValue(true);

                        mFirebaseDatabase.child(loggedInUser.opponentID).child("accepted").setValue("true");

                        // navigate to game screen
                        startActivity(new Intent(ChoosePlayerActivity.this, GameActivity.class));
                    }
                });

        // Setting Negative "NO" Btn
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mFirebaseDatabase.child(loggedInUser.myID).child("opponentID").setValue("");
                        mFirebaseDatabase.child(loggedInUser.myID).child("opponentEmail").setValue("");
                        mFirebaseDatabase.child(loggedInUser.opponentID).child("accepted").setValue("false");
                        dialog.cancel();
                    }
                });

        // Showing Alert Dialog
        alertDialog.show();
    }

    public void onClickInvite(View view) {
        if (currentOpponent != null) {
            // set opponent id for selected user to invite and let them know they have an invite in database
            mFirebaseDatabase.child(currentOpponent.myID).child("opponentID").setValue(loggedInUser.myID);
            mFirebaseDatabase.child(currentOpponent.myID).child("opponentEmail").setValue(loggedInUser.email);
            mFirebaseDatabase.child(currentOpponent.myID).child("request").setValue(true);

            // set opponent id for current logged in user in database
            mFirebaseDatabase.child(loggedInUser.myID).child("opponentID").setValue(currentOpponent.myID);
            mFirebaseDatabase.child(loggedInUser.myID).child("opponentEmail").setValue(currentOpponent.email);
            mFirebaseDatabase.child(loggedInUser.myID).child("accepted").setValue("pending");

            progressBar.setVisibility(View.VISIBLE);
            inviteButton.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(ChoosePlayerActivity.this, MainActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

