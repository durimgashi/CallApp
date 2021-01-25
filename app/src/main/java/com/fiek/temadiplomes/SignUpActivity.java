package com.fiek.temadiplomes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fiek.temadiplomes.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailReg, usernameReg, passwordReg;
    private Button btnSignUp;
    private FirebaseAuth firebaseAuth;
    private int RC_SIGN_IN = 1;
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
        logInLink.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, MainActivity.class)));

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
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                //Saving user to collection
                                userId = firebaseAuth.getCurrentUser().getUid();
                                User user = new User(firebaseAuth.getCurrentUser().getEmail(), username, null, false);
                                saveUserToFirestore(user, userId);

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("username", firebaseAuth.getCurrentUser().getUid());
                                startActivity(intent);
                            }else{
                                Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }

    public void saveUserToFirestore(User user, String userId){
        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SignUpActivity.this, "User created", Toast.LENGTH_SHORT).show();
            }
        });
    }
}