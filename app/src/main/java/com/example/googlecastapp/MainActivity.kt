package com.example.googlecastapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.IntroductoryOverlay
import androidx.mediarouter.app.MediaRouteButton

class MainActivity : AppCompatActivity() {
    private var castSession: CastSession? = null
    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            castSession = session
            loadRemoteMedia()
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            castSession = null
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            castSession = session
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionResumeFailed(session: CastSession, error: Int) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
        override fun onSessionStarting(p0: CastSession) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            val castContext = CastContext.getSharedInstance(this)
            castContext.sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        } catch (e: RuntimeException) {
            Log.e("MainActivity", "Failed to initialize CastContext", e)
        }


        val mediaRouteButton: MediaRouteButton = findViewById(R.id.btn_cast)
        CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton)

        showIntroductoryOverlay(mediaRouteButton)
    }

    private fun loadRemoteMedia() {
        val mediaInfo = MediaInfo.Builder("https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5")
            .setContentType("video/mp4")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .build()

        castSession?.remoteMediaClient?.load(
            mediaInfo,
            MediaLoadOptions.Builder().setAutoplay(true).build()
        )?.setResultCallback { result ->
            if (result.status.isSuccess) {
                Log.d("MainActivity", "Media loaded")
            } else {
                Log.e("MainActivity", "Error: ${result.status}")
            }
        }
    }

    private fun showIntroductoryOverlay(mediaRouteButton: MediaRouteButton) {
        val castContext = CastContext.getSharedInstance(this)
        if (castContext.sessionManager.currentCastSession == null) {
            IntroductoryOverlay.Builder(this, mediaRouteButton)
                .setTitleText("Tap to Cast")
                .setSingleTime()
                .build()
                .show()
        }
    }

    override fun onDestroy() {
        CastContext.getSharedInstance(this).sessionManager.removeSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
        super.onDestroy()
    }
}