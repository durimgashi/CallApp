package com.fiek.temadiplomes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fiek.temadiplomes.Interfaces.JavaScriptInterface;
import com.fiek.temadiplomes.Interfaces.VoiceCallInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class VoiceCallActivity extends AppCompatActivity {
    private WebView webView;
    private Boolean isPeerConnencted = false;
    private CollectionReference firebaseRef = FirebaseFirestore.getInstance().collection("users");
    private Boolean isAudio = true;
    private Boolean isVideo = true;
    private String friendUID, userUID;
    private TextView endCall;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicecall_layout);

        webView = findViewById(R.id.voicecallWV);
        endCall = findViewById(R.id.endCall);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        userUID = FirebaseAuth.getInstance().getUid();
        friendUID = getIntent().getStringExtra("friendUID");

        sendCallRequest();

        setupWebView();
        monitorCallAnswer();

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endItAll();
                finish();
            }
        });

        Button test = findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callJavaScriptFunction("javascript:toggleVideo(false)");
                webView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void sendCallRequest() {
//        if (!isPeerConnencted){
//            Toast.makeText(VideoCallActivity.this, "Not connected!", Toast.LENGTH_SHORT).show();
//            return;
//        }
        Toast.makeText(VoiceCallActivity.this, friendUID, Toast.LENGTH_LONG).show();
        ref.child(friendUID).child("incoming").setValue(userUID);

        listenForConnectionId();
    }

    private void listenForConnectionId() {
        callJavaScriptFunction("javascript:startCall('" + friendUID + "')");
    }

    private void monitorCallAnswer(){
        ref.child(userUID).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().equals(friendUID)){
                    Toast.makeText(VoiceCallActivity.this, "Timer starts here", Toast.LENGTH_LONG).show();
                    ref.removeEventListener(this);
                    startCheckingForEnd();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startCheckingForEnd(){
        ref.child(friendUID).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.getValue().equals(userUID)){
                    Toast.makeText(VoiceCallActivity.this, "Call has ended!", Toast.LENGTH_LONG).show();
                    ref.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endItAll();
    }

    public void endItAll(){
        webView.loadUrl("");
        ref.child(userUID).child("incoming").setValue("");
        ref.child(friendUID).child("incoming").setValue("");
        startActivity(new Intent(VoiceCallActivity.this, ContactsActivity.class));
    }
}
