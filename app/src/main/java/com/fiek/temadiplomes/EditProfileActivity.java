package com.fiek.temadiplomes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.fiek.temadiplomes.Model.Upload;
import com.fiek.temadiplomes.Utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();
    private de.hdodenhof.circleimageview.CircleImageView profileImage;
    private FloatingActionButton editProfileImage;
    public static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Button saveChanges;
    private EditText username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_layout);

        profileImage = findViewById(R.id.profileImage);
        editProfileImage = findViewById(R.id.editProfileImage);
        saveChanges = findViewById(R.id.saveChanges);
        editProfileImage.setOnClickListener(v -> openFileChooser());
        saveChanges.setOnClickListener(v -> saveImage());
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);

        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.IMAGE_FIELD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = Objects.requireNonNull(snapshot.getValue()).toString();
                Picasso.get().load(imageUrl).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.USERNAME_FIELD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                username.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFileExtention(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void saveImage() {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                final UploadTask uploadTask = fileReference.putFile(imageUri);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Upload upload = new Upload(UUID.randomUUID().toString(), task.getResult().toString());
                        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.IMAGE_FIELD).setValue(upload.getUrl());
                        Toast.makeText(EditProfileActivity.this, "Changes have been saved.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Upload has failed!", Toast.LENGTH_LONG).show()).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                    double progress = (100.0 * snapshot.getBytesTransferred()/ snapshot.getTotalByteCount());
//                    progressBar.setProgress((int) progress);
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            Picasso.get().load(imageUri).into(profileImage);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditProfileActivity.this, ContactsActivity.class));
    }
}