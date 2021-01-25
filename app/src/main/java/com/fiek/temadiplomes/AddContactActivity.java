package com.fiek.temadiplomes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fiek.temadiplomes.Adapters.ContactAdapter;
import com.fiek.temadiplomes.Adapters.NewContactAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddContactActivity extends AppCompatActivity {
    private List<String> users = new ArrayList<>();
    private NewContactAdapter adapter;
    private EditText searchBar;
    private Button filterBtn;
    private String currUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcontact_layout);

        searchBar = findViewById(R.id.usernameAdd);
        loadContacts("");


        currUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Toast.makeText(AddContactActivity.this, "" + currUserEmail, Toast.LENGTH_SHORT).show();

        searchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    loadContacts(s.toString());
                else
                    loadContacts("");
            }
        });
    }

    private void loadContacts(String keyword) {
        RecyclerView contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        Query docRef;
        if (keyword.equals("")){
//            docRef = FirebaseFirestore.getInstance().collection("users").whereNotEqualTo("email", currUserEmail);
            docRef = FirebaseFirestore.getInstance().collection("users").whereNotEqualTo("email", currUserEmail);
        } else {
            docRef = FirebaseFirestore.getInstance().collection("users")
                    .whereGreaterThanOrEqualTo("username", keyword)
                    .whereLessThanOrEqualTo("username", keyword + "z")
                    .whereNotEqualTo("email", currUserEmail);
        }
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    contactsRecyclerView.setLayoutManager(new LinearLayoutManager(AddContactActivity.this));
                    adapter = new NewContactAdapter(AddContactActivity.this, list);
                    contactsRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(AddContactActivity.this, "Could not get users!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddContactActivity.this, ContactsActivity.class));
    }
}