package com.yogeshkulkarni.firebasetictactoe;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private EditText ie,ip,iu;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabaseInstance;
    private DatabaseReference mFirebaseDatabase;
    private String userId;
    private String Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iu = findViewById(R.id.username);
        ie = findViewById(R.id.email);
        ip =findViewById(R.id.password);

        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabaseInstance = FirebaseDatabase.getInstance();
        if(mAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(MainActivity.this,ChoosePlayerActivity.class));
            finish();
        }

    }


    public void onRegisterClicked(View view)
    {
        final String email = ie.getText().toString().trim();
        String password = ip.getText().toString().trim();
        final String username = iu.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(getApplicationContext(), "Enter username!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(MainActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Authentication failed." + task.getException(),
                            Toast.LENGTH_LONG).show();
                    Log.e("MyTag", task.getException().toString());
                } else {

                    mFirebaseDatabase = mFirebaseDatabaseInstance.getReference("users");


                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    Email = user.getEmail();
                    User  myUser = new User(username,Email,userId);

                    mFirebaseDatabase.child(userId).setValue(myUser);

                    startActivity(new Intent(MainActivity.this, ChoosePlayerActivity.class));
                    finish();
                }
            }

        });
    }
    public void onLoginClicked(View view)
    {
        startActivity(new Intent(this, LoginActivity.class));
    }

}
