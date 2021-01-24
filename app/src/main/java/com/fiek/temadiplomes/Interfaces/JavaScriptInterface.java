package com.fiek.temadiplomes.Interfaces;
import com.fiek.temadiplomes.VideoCallActivity;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

public class JavaScriptInterface {
//    @NotNull
    private VideoCallActivity callActivity;

    @android.webkit.JavascriptInterface
    public final void onPeerConnected() {
        this.callActivity.onPeerConnected();
    }

    public JavaScriptInterface(@NotNull VideoCallActivity callActivity) {
        this.callActivity = callActivity;
    }
}
