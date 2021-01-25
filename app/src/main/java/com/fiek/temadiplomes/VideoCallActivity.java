package com.fiek.temadiplomes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fiek.temadiplomes.Interfaces.JavaScriptInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class VideoCallActivity extends AppCompatActivity {

    private WebView webView;
    private Boolean isPeerConnencted = false;
    private CollectionReference firebaseRef = FirebaseFirestore.getInstance().collection("users");
    private Boolean isAudio = true;
    private Boolean isVideo = true;
    private String friendUID, userUID;
    private TextView endCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videocall_layout);

        webView = findViewById(R.id.videocallWV);
        endCall = findViewById(R.id.endCall);

        userUID = FirebaseAuth.getInstance().getUid();
        friendUID = getIntent().getStringExtra("friendUID");

        //Toast.makeText(VideoCallActivity.this, userUID + " ///" + friendUID, Toast.LENGTH_LONG).show();
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
    }

    private void sendCallRequest() {
//        if (!isPeerConnencted){
//            Toast.makeText(VideoCallActivity.this, "Not connected!", Toast.LENGTH_SHORT).show();
//            return;
//        }
        firebaseRef.document(friendUID).update("incoming", userUID);
        
        listenForConnectionId();
    }

    private void listenForConnectionId() {
        callJavaScriptFunction("javascript:startCall('" + friendUID + "')");
    }

    private void monitorCallAnswer(){
        firebaseRef.document(Objects.requireNonNull(friendUID))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        assert value != null;
                        String incoming = value.get("incoming").toString();
                        if(incoming.equals("")){
                            endItAll();
                        }
                    }
                });
    }

//    @SuppressLint("SetJavaScriptEnabled")
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
        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

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

        firebaseRef.document(userUID)
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    assert value != null;
                    String incoming = value.get("incoming").toString();
                    if(!incoming.equals("")){
//                        Toast.makeText(VideoCallActivity.this, incoming + " is calling you", Toast.LENGTH_LONG).show();
                    }
                }
        });
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
        firebaseRef.document(userUID).update("incoming", "");
        firebaseRef.document(friendUID).update("incoming", "");
        startActivity(new Intent(VideoCallActivity.this, ContactsActivity.class));
    }
}