package com.fiek.temadiplomes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.ktx.FirebaseKt;

public class SignInActivity extends AppCompatActivity {
    private TextView signUpLink;
    private Button logInButton;
    private EditText etUsername, etPassword;
    private Integer requestcode = 1;
    private FirebaseAuth firebaseAuth;
    private final String[] permissions = {
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isPermissionGranted()){
            askPermissions();
        }

        FirebaseKt.initialize(Firebase.INSTANCE, this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(SignInActivity.this, ContactsActivity.class);
            intent.putExtra("username", firebaseAuth.getCurrentUser().getUid());
            startActivity(intent);
            finish();
        }

        signUpLink = findViewById(R.id.signUpLink);
        logInButton = findViewById(R.id.logInButton);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        signUpLink.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));

        logInButton.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(SignInActivity.this, ContactsActivity.class);
                        intent.putExtra("username", firebaseAuth.getCurrentUser().getUid());
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignInActivity.this, "Error" + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private boolean isPermissionGranted() {
        for (String perm : permissions){
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void askPermissions(){
        ActivityCompat.requestPermissions(this, permissions, requestcode);
    }
}