package io.github.jdanders.dropcount

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrientationLockTest {
    @Test
    fun testMainActivityIsPortraitOnly() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = context.packageManager
        val componentName = android.content.ComponentName(context, MainActivity::class.java)
        
        val activityInfo = packageManager.getActivityInfo(componentName, 0)
        
        assertEquals(
            "MainActivity should be locked to portrait orientation",
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            activityInfo.screenOrientation
        )
    }
}
