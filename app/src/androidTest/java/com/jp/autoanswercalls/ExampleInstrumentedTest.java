package com.jp.autoanswercalls;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.jp.autoanswercalls.utils.BaiduVoiceUtil;
import com.jp.autoanswercalls.utils.RecordAudioUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.jp.autoanswercalls", appContext.getPackageName());

        String responseJson = BaiduVoiceUtil.miniRecognize("/storage/emulated/0/CallRec/CallRec_2017-09-05-14-37-57__incomingcall.amr");
        Log.d(RecordAudioUtil.class.getName(), responseJson);
    }
}
