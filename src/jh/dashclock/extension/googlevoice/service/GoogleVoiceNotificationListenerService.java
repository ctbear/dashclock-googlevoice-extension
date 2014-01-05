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

import android.content.ContentValues;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.apps.dashclock.api.DashClockExtension;
import jh.dashclock.extension.googlevoice.R;
import jh.dashclock.extension.googlevoice.Utils;
import jh.dashclock.extension.googlevoice.storage.MessageSQLiteHelper;
import jh.dashclock.extension.googlevoice.storage.SQLStorageUtils;

import java.util.List;

/**
 * Notification listener service
 */
public class GoogleVoiceNotificationListenerService extends NotificationListenerService implements IGoogleVoiceService {
    public static final String TAG = "GoogleVoiceNotificationListenerService";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(getString(R.string.google_voice_package)) &&
                !Utils.isAccessibilityServiceOn(getApplicationContext())) {
            ContentValues values = new ContentValues();
            values.put(MessageSQLiteHelper.COLUMN_MESSAGE, sbn.getNotification().tickerText.toString());
            values.put(MessageSQLiteHelper.COLUMN_TIMESTAMP, sbn.getPostTime());
            SQLStorageUtils.insert(getContentResolver(), values);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(getString(R.string.google_voice_package))) {
            try {
                // Previous active notification is removed before new one is posted
                // Need to wait for new one to be posted before checking active notifications
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while sleeping: " + e.getLocalizedMessage());
            }

            boolean foundActiveNotification = false;
            for (StatusBarNotification notification : getActiveNotifications()) {
                if (notification.getPackageName().equals(getString(R.string.google_voice_package))) {
                    foundActiveNotification = true;
                    break;
                }
            }
            if (!foundActiveNotification) {
                // No Google Voice active notifications means they are cleared by user
                SQLStorageUtils.deleteAllRows(getContentResolver());
            }
        }
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
        onDestroy();
    }
}
