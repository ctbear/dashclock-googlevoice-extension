/**
 * Copyright 2013 Jerry Hung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jh.dashclock.extension.googlevoice.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.google.android.apps.dashclock.api.DashClockExtension;
import jh.dashclock.extension.googlevoice.R;
import jh.dashclock.extension.googlevoice.Utils;
import jh.dashclock.extension.googlevoice.storage.MessageSQLiteHelper;
import jh.dashclock.extension.googlevoice.storage.SQLStorageUtils;

import java.util.List;

/**
 * Accessibility Service
 * Monitors Google Voice package and performs CRUD through content provider
 */
public class GoogleVoiceAccessibilityService extends AccessibilityService implements IGoogleVoiceService {
    public static final String TAG = "GoogleVoiceAccessibilityService";

    private static boolean isInit = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Insert to or clear database base on event type
        String packageName = String.valueOf(event.getPackageName());
        if (packageName.equals(getString(R.string.google_voice_package))) {
            int eventType = event.getEventType();
            if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                insert(event);
            } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                SQLStorageUtils.deleteAllRows(getContentResolver());
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        if (!isInit) {
            Log.d(TAG, "onServiceConnected");
            isInit = true;
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupted");
        isInit = false;
    }

    @Override
    public int getUnreadCount(DashClockExtension mContext) {
        return SQLStorageUtils.getCountFromProvider(mContext.getContentResolver());
    }

    @Override
    public String getSender(DashClockExtension mContext) {
        String text = SQLStorageUtils.getLastMessageFromProvider(mContext.getContentResolver());
        // Message sender
        if (!TextUtils.isEmpty(text) && text.contains(":")) {
            return Utils.parseSender(text);
        } else {
            return "";
        }
    }

    @Override
    public String getBody(DashClockExtension mContext) {
        String text = SQLStorageUtils.getLastMessageFromProvider(mContext.getContentResolver());
        // Message body
        if (!TextUtils.isEmpty(text) && text.contains(":")) {
            return Utils.parseBody(text);
        } else {
            return "";
        }
    }

    @Override
    public List<String> getAllMessages(DashClockExtension mContext) {
        return SQLStorageUtils.getAllMessagesFromProvider(mContext.getContentResolver());
    }

    @Override
    public void destroy() {
        onInterrupt();
    }

    private void insert(AccessibilityEvent event) {
        ContentValues values = new ContentValues();
        values.put(MessageSQLiteHelper.COLUMN_MESSAGE, TextUtils.join("", event.getText().toArray()));
        values.put(MessageSQLiteHelper.COLUMN_TIMESTAMP, event.getEventTime());
        SQLStorageUtils.insert(getContentResolver(), values);
    }
}
