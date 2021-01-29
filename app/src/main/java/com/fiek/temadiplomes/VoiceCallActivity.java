package com.fiek.temadiplomes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Chronometer;
import android.widget.TextView;

import com.fiek.temadiplomes.Interfaces.VoiceCallInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VoiceCallActivity extends AppCompatActivity {
    private WebView webView;
    private Boolean isPeerConnencted = false;
    private Boolean isAudio = true;
    private Boolean isVideo = true;
    private Boolean speakerON = false;
    private Boolean micON = false;
    private String friendUID, userUID;
    private TextView endCall, speaker, microphone;
    private ConstraintLayout voiceCallConstraintLayout;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();
    private AnimationDrawable animationDrawable;
    private Chronometer simpleChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicecall_layout);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        simpleChronometer = findViewById(R.id.counter);
        webView = findViewById(R.id.voicecallWV);
        webView.setVisibility(View.INVISIBLE);
        endCall = findViewById(R.id.endCall);
        speaker = findViewById(R.id.speaker);
        microphone = findViewById(R.id.microphone);
        voiceCallConstraintLayout = findViewById(R.id.voiceCallConstraintLayout);

        animationDrawable = (AnimationDrawable) voiceCallConstraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(3000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        userUID = FirebaseAuth.getInstance().getUid();
        friendUID = getIntent().getStringExtra("friendUID");

        if (!friendUID.equals("") || friendUID != null || friendUID.length() < 5){
            sendCallRequest();
            setupWebView();
        }

        endCall.setOnClickListener(v -> endItAll());
        speaker.setOnClickListener(v -> toggleSpeaker());
        microphone.setOnClickListener(v -> toggleMic());
    }

    private void toggleSpeaker(){
        speakerON = !speakerON;
        if (speakerON) speaker.setBackground(ContextCompat.getDrawable(VoiceCallActivity.this, R.drawable.ic_speakeron));
        else speaker.setBackground(ContextCompat.getDrawable(VoiceCallActivity.this, R.drawable.ic_speakeroff));
    }

    private void toggleMic(){
        micON = !micON;
        if (micON) microphone.setBackground(ContextCompat.getDrawable(VoiceCallActivity.this, R.drawable.ic_micon));
        else microphone.setBackground(ContextCompat.getDrawable(VoiceCallActivity.this, R.drawable.ic_micoff));
    }

    private void sendCallRequest() {
        ref.child(friendUID).child(Constants.INCOMING_VOICE_FIELD).setValue(userUID);
        listenForConnectionId();
    }

    private void listenForConnectionId() {
        callJavaScriptFunction("javascript:startCall('" + friendUID + "')");
        callJavaScriptFunction("javascript:toggleVideo(false)");
        monitorCallAnswer();
        startCheckingForEnd();
    }

    private void monitorCallAnswer(){
        ref.child(userUID).child(Constants.INCOMING_VOICE_FIELD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().equals(friendUID)){
                    findViewById(R.id.textToCounter).setVisibility(View.INVISIBLE);
                    simpleChronometer.setVisibility(View.VISIBLE);
                    simpleChronometer.setBase(SystemClock.elapsedRealtime());
                    simpleChronometer.start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startCheckingForEnd(){
        ref.child(friendUID).child(Constants.INCOMING_VOICE_FIELD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.getValue().equals(userUID)){
//                    Toast.makeText(VoiceCallActivity.this, "Call has ended!", Toast.LENGTH_LONG).show();
                    ref.removeEventListener(this);
                    endItAll();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(){
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (request != null) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new VoiceCallInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                initializePeer();
            }
        });
    }

    public void initializePeer(){
        callJavaScriptFunction("javascript:init('" + userUID + "')");
    }

    private void callJavaScriptFunction(String functionString){
        webView.post(() -> webView.evaluateJavascript(functionString, null));
    }

    public void onPeerConnected() {
        isPeerConnencted = true;
    }

    public void endItAll(){
        webView.loadUrl("about:blank");
        Intent intent = new Intent(VoiceCallActivity.this, ContactsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.INCOMING_VOICE_FIELD).setValue("");
        ref.child(friendUID).child(Constants.INCOMING_VOICE_FIELD).setValue("");
        finish();
    }
}
