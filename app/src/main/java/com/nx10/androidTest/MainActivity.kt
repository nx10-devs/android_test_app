package com.nx10.androidTest

import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nx10.androidTest.ui.TestHarnessScreen
import com.nx10.androidTest.ui.theme.NX10TestAppTheme
import com.nx10.asparagus.AsparagusSdk
import com.nx10.asparagus.SdkSessionState

class MainActivity : ComponentActivity() {
    private lateinit var sdk: AsparagusSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdk = AsparagusSdk.getOrCreate(applicationContext)
        if (savedInstanceState == null &&
            BuildConfig.DEFAULT_API_KEY.isNotBlank() &&
            sdk.statusFlow.value.sessionState == SdkSessionState.IDLE
        ) {
            sdk.startSession(apiKey = BuildConfig.DEFAULT_API_KEY)
        }
        enableEdgeToEdge()
        setContent {
            NX10TestAppTheme {
                TestHarnessScreen(sdk = sdk)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        sdk.observeMotionEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (::sdk.isInitialized) {
            sdk.onWindowFocusChanged(hasFocus)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::sdk.isInitialized) {
            sdk.onScreenChanged()
        }
    }

    override fun onDestroy() {
        if (isFinishing && ::sdk.isInitialized) {
            sdk.onAppQuitting()
        }
        super.onDestroy()
    }
}
