package com.fiek.temadiplomes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fiek.temadiplomes.Model.User;
import com.fiek.temadiplomes.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailReg, usernameReg, passwordReg;
    private Button btnSignUp;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameReg = findViewById(R.id.usernameReg);
        emailReg = findViewById(R.id.emailReg);
        passwordReg = findViewById(R.id.passwordReg);
        btnSignUp = findViewById(R.id.btnSignUp);

        TextView logInLink = findViewById(R.id.logInLink);
        logInLink.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));

        btnSignUp.setOnClickListener(v -> {
            final String email = emailReg.getText().toString();
            final String password = passwordReg.getText().toString();
            final String username = usernameReg.getText().toString();

            if (TextUtils.isEmpty(email)){
                emailReg.setError("Email is empty!");
                return;
            }
            if (TextUtils.isEmpty(password)){
                passwordReg.setError("Email is empty!");
            }
            if(TextUtils.isEmpty(username)){
                usernameReg.setError("Username is empty");
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            userId = firebaseAuth.getCurrentUser().getUid();
//                                List<String> durimDefaulltFriend = new ArrayList<>();
//                                durimDefaulltFriend.add("yVln3seYb5UEqiY0VQWato2nxS02");
                            User user = new User(firebaseAuth.getCurrentUser().getEmail(),
                                                    username,
                                                    null,
                                                    false,
                                                    "",
                                                    "",
                                                    Constants.DEFAULT_IMAGE_TOKEN);
                            saveUserToFirestore(user, userId);

                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            intent.putExtra("username", firebaseAuth.getCurrentUser().getUid());
                            startActivity(intent);
                        }else{
                            Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    public void saveUserToFirestore(User user, String userId){
        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.set(user).addOnSuccessListener(aVoid -> Toast.makeText(SignUpActivity.this, "User created", Toast.LENGTH_SHORT).show());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        ref.child(userId).setValue(user);
    }
}