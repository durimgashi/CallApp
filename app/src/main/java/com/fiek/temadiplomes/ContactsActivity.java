package com.fiek.temadiplomes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fiek.temadiplomes.Adapters.ContactAdapter;
import com.fiek.temadiplomes.Notifications.App;
import com.fiek.temadiplomes.Utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private ContactAdapter adapter;
    private String userUID;
    private TextView logOutButton;
    private FloatingActionButton addContactFAB;
    private RelativeLayout voiceCallNotification, videoCallNotification;
    private ImageView rejectVoiceBtn, answerVoiceBtn, rejectVideoBtn, answerVideoBtn;
    private Vibrator myVib;
    private TextView incomingVoiceCallTxt, incomingVideoCallTxt, usernameCurr;
    private de.hdodenhof.circleimageview.CircleImageView userProfile;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();
    private String incomingVoiceUID, incomingVideoUID;
    private NotificationManagerCompat notificationManagerCompat;
    private LinearLayout editLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_layout);

        notificationManagerCompat = NotificationManagerCompat.from(ContactsActivity.this);

        logOutButton = findViewById(R.id.logOutButton);
        addContactFAB = findViewById(R.id.addContactFAB);
        voiceCallNotification = findViewById(R.id.voiceCallNotification);
        videoCallNotification = findViewById(R.id.videoCallNotification);
        rejectVoiceBtn = findViewById(R.id.rejectVoiceBtn);
        answerVoiceBtn = findViewById(R.id.answerVoiceBtn);
        rejectVideoBtn = findViewById(R.id.rejectVideoBtn);
        answerVideoBtn = findViewById(R.id.answerVideoBtn);
        incomingVoiceCallTxt = findViewById(R.id.incomingVoiceCallTxt);
        incomingVideoCallTxt = findViewById(R.id.incomingVideoCallTxt);
        usernameCurr = findViewById(R.id.usernameCurr);
        userProfile = findViewById(R.id.userProfile);
        editLayout = findViewById(R.id.editLayout);

        userUID = FirebaseAuth.getInstance().getUid();

        myVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loadContacts();
        monitorCalls();

        ref.child(userUID).child(Constants.AVAILABLE_FIELD).setValue(true);

        ref.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usernameCurr.setText(snapshot.child(Constants.USERNAME_FIELD).getValue().toString());
                Picasso.get().load(snapshot.child(Constants.IMAGE_FIELD).getValue().toString()).into(userProfile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editLayout.setOnClickListener(view -> startActivity(new Intent(ContactsActivity.this, EditProfileActivity.class)));

        rejectVoiceBtn.setOnClickListener(v -> {
            ref.child(userUID).child(Constants.INCOMING_VOICE_FIELD).setValue("");
            voiceCallNotification.setVisibility(View.GONE);
            myVib.cancel();
        });

        answerVoiceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ContactsActivity.this, VoiceCallActivity.class);
            intent.putExtra("friendUID", incomingVoiceUID);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        rejectVideoBtn.setOnClickListener(v -> {
            ref.child(userUID).child(Constants.INCOMING_VIDEO_FIELD).setValue("");
            voiceCallNotification.setVisibility(View.GONE);
            myVib.cancel();
        });

        answerVideoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ContactsActivity.this, VideoCallActivity.class);
            intent.putExtra("friendUID", incomingVideoUID);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        logOutButton.setOnClickListener(e -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ContactsActivity.this, SignInActivity.class));
        });

        addContactFAB.setOnClickListener(v -> {
            startActivityForResult(new Intent(ContactsActivity.this, AddContactActivity.class), RESULT_OK);
            finish();
        });
    }

    private void monitorCalls(){
        //He is the chosen one, Obi-Wan
        ref.child(FirebaseAuth.getInstance().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getKey().equals(Constants.INCOMING_VIDEO_FIELD)){
                    if (!snapshot.getValue().equals("")){
                        videoCallNotification.setVisibility(View.VISIBLE);
                        getCallerUsername(snapshot.getValue().toString(), Constants.VIDEO_TYPE);
                        incomingVideoUID = snapshot.getValue().toString();
                    } else if (snapshot.getValue().toString().equals("")){
                        videoCallNotification.setVisibility(View.GONE);
                    }
                } else if(snapshot.getKey().equals(Constants.INCOMING_VOICE_FIELD)){
                    if (!snapshot.getValue().equals("")){
                        voiceCallNotification.setVisibility(View.VISIBLE);
                        getCallerUsername(snapshot.getValue().toString(), Constants.VOICE_TYPE);
                        incomingVoiceUID = snapshot.getValue().toString();
                    } else if (snapshot.getValue().toString().equals("")){
                        voiceCallNotification.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
                if (type.equals(Constants.VOICE_TYPE))
                    incomingVoiceCallTxt.setText(snapshot.getValue().toString() + " is calling you...");
                else if(type.equals(Constants.VIDEO_TYPE))
                    incomingVideoCallTxt.setText(snapshot.getValue().toString() + " is calling you...");

                sendOnChannel1(snapshot.getValue().toString() + " is calling you...", uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendOnChannel1(String text, String callerUid){
//        Intent answerVoiceIntent = new Intent(ContactsActivity.this, VoiceCallActivity.class);
//        answerVoiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        answerVoiceIntent.setAction(Intent.ACTION_ANSWER);
//        answerVoiceIntent.putExtra("friendUID", callerUid);
//        PendingIntent pendingVoiceIntent = PendingIntent.getActivity(this, 0, answerVoiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        Notification notification = new NotificationCompat.Builder(ContactsActivity.this, App.CHANNEL_1_ID)
//                .setSmallIcon(R.drawable.bird)
//                .setContentTitle(text)
//                .setCategory(NotificationCompat.CATEGORY_CALL)
//                .setContentIntent(pendingVoiceIntent)
//                .addAction(R.drawable.answercall, "Answer", pendingVoiceIntent)
//                .build();
//        notificationManagerCompat.notify(1, notification);
    }

    public RecyclerView contactsRecyclerView;

    public void loadContacts() {
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        ref.child(userUID).child(Constants.FRIENDS_FILED).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChildren()){
                    List<String> friends = new ArrayList<>();
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        friends.add(postSnapshot.getValue().toString());
                    }
                    contactsRecyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
                    adapter = new ContactAdapter(ContactsActivity.this, friends);
                    contactsRecyclerView.setAdapter(adapter);
                } else {
                    contactsRecyclerView.setVisibility(View.GONE);
                    findViewById(R.id.noCont).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.child(userUID).child(Constants.AVAILABLE_FIELD).setValue(false);
    }
}