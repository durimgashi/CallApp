package com.fiek.temadiplomes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fiek.temadiplomes.Adapters.ContactAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ContactsActivity extends AppCompatActivity {

    private ContactAdapter adapter;
    private List<String> friends =  new ArrayList<>();
    private String userUID;
    private TextView logOutButton;
    private FloatingActionButton addContactFAB;
    private CollectionReference firebaseRef = FirebaseFirestore.getInstance().collection("users");
    private RelativeLayout callNotification;
    private ImageView rejectBtn, answerBtn;
    private Vibrator myVib;
    private TextView incomingCallTxt;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();

    private String incomingUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_layout);

        logOutButton = findViewById(R.id.logOutButton);
        addContactFAB = findViewById(R.id.addContactFAB);
        callNotification = findViewById(R.id.callNotification);
        rejectBtn = findViewById(R.id.rejectBtn);
        answerBtn = findViewById(R.id.answerBtn);
        incomingCallTxt = findViewById(R.id.incomingCallTxt);

        userUID = FirebaseAuth.getInstance().getUid();
        myVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loadContacts();
        monitorCalls();


        rejectBtn.setOnClickListener(v -> {
            ref.child(userUID).child(Constants.INCOMING_FIELD).setValue("");
            callNotification.setVisibility(View.GONE);
            myVib.cancel();
        });

        answerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ContactsActivity.this, VoiceCallActivity.class);
            intent.putExtra("friendUID", incomingUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        logOutButton.setOnClickListener(e -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ContactsActivity.this, MainActivity.class));
        });

        addContactFAB.setOnClickListener(v -> {
            startActivityForResult(new Intent(ContactsActivity.this, AddContactActivity.class), RESULT_OK);
            finish();
        });
    }

    private void monitorCalls(){
        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.INCOMING_FIELD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                assert value != null;
                if (!value.equals("")){
                    callNotification.setVisibility(View.VISIBLE);
                    getCallerUsername(value, Constants.VIDEO_TYPE);
                    incomingUid = value;
                } else {
                    callNotification.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.INCOMING_VOICE_FIELD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                assert value != null;
                if (!value.equals("")){
                    callNotification.setVisibility(View.VISIBLE);
                    getCallerUsername(value, Constants.VOICE_TYPE);
                } else {
                    callNotification.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCallerUsername(String uid, String type){
        ref.child(uid).child("username").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                incomingCallTxt.setText(snapshot.getValue().toString() + " is calling you. (" + type + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public RecyclerView contactsRecyclerView;

    public void loadContacts() {
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(userUID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> note = documentSnapshot.getData();
                        assert note != null;
                        friends = (List<String>) note.get("friends");

                        if(friends != null){
                            contactsRecyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
                            adapter = new ContactAdapter(ContactsActivity.this, friends);
                            contactsRecyclerView.setAdapter(adapter);
                        }
                    } else {
                        Toast.makeText(ContactsActivity.this, "You might have no friends!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ContactsActivity.this, "Error!", Toast.LENGTH_SHORT).show());
    }
}