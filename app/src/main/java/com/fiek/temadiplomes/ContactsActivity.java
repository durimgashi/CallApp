package com.fiek.temadiplomes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fiek.temadiplomes.Adapters.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    private List<String> friends =  new ArrayList<>();
    private String userUID;
    private TextView logOutButton;
    private FloatingActionButton addContactFAB;
    private CollectionReference firebaseRef = FirebaseFirestore.getInstance().collection("users");
    private RelativeLayout callNotification;
    private ImageView rejectBtn, answerBtn;
    private Vibrator myVib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_layout);

        logOutButton = findViewById(R.id.logOutButton);
        addContactFAB = findViewById(R.id.addContactFAB);
        callNotification = findViewById(R.id.callNotification);

        rejectBtn = findViewById(R.id.rejectBtn);
        answerBtn = findViewById(R.id.answerBtn);

//        userUID = getIntent().getStringExtra("username");
        userUID = FirebaseAuth.getInstance().getUid();

        loadContacts();

        myVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        monitorCalls();

        rejectBtn.setOnClickListener(v -> {
            firebaseRef.document(userUID).update("incoming", "");
            callNotification.setVisibility(View.GONE);
            myVib.cancel();
        });

        answerBtn.setOnClickListener(v -> {
            firebaseRef.document(userUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Intent intent = new Intent(ContactsActivity.this, VideoCallActivity.class);
                    intent.putExtra("friendUID", documentSnapshot.get("incoming").toString());
                    startActivity(intent);
                }
            });
        });

        logOutButton.setOnClickListener(e -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ContactsActivity.this, MainActivity.class));
        });
    }

    private void monitorCalls(){
        firebaseRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        assert value != null;
                        String incoming = value.get("incoming").toString();
                        if(!incoming.equals("")){
//                            Toast.makeText(VideoCallActivity.this, incoming + " is calling you", Toast.LENGTH_LONG).show();
                            callNotification.setVisibility(View.VISIBLE);
//                            myVib.vibrate(1000);
                        }
                    }
                });
    }

    private void onCallRequest(){

    }


    private void loadContacts() {
        RecyclerView contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(userUID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> note = documentSnapshot.getData();
                        assert note != null;
                        friends = (List<String>) note.get("friends");

                        if(friends != null){
                            contactsRecyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
                            adapter = new RecyclerViewAdapter(ContactsActivity.this, friends);
                            contactsRecyclerView.setAdapter(adapter);
                        }
                    } else {
                        Toast.makeText(ContactsActivity.this, "You might have no friends!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ContactsActivity.this, "Error!", Toast.LENGTH_SHORT).show());
    }
}