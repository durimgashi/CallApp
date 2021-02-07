package com.fiek.temadiplomes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import com.fiek.temadiplomes.Adapters.NewContactAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {
    private NewContactAdapter adapter;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcontact_layout);
        loadContacts();
    }

    private void loadContacts() {
        RecyclerView contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> users = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    users.add(postSnapshot.getKey());
                }
                contactsRecyclerView.setLayoutManager(new LinearLayoutManager(AddContactActivity.this));
                adapter = new NewContactAdapter(AddContactActivity.this, users);
                contactsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddContactActivity.this, ContactsActivity.class));
    }
}