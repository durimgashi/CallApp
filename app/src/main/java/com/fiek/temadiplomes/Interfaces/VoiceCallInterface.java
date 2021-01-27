package com.fiek.temadiplomes.Interfaces;
import com.fiek.temadiplomes.VideoCallActivity;
import com.fiek.temadiplomes.VoiceCallActivity;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

public class VoiceCallInterface {
    //    @NotNull
    private VoiceCallActivity callActivity;

    @android.webkit.JavascriptInterface
    public final void onPeerConnected() {
        this.callActivity.onPeerConnected();
    }

    public VoiceCallInterface(@NotNull VoiceCallActivity callActivity) {
        this.callActivity = callActivity;
    }
}
