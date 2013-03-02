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

package jh.dashclock.extension.googlevoice;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import jh.dashclock.extension.googlevoice.provider.MessageContentProvider;
import jh.dashclock.extension.googlevoice.storage.MessageSQLiteHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Accessibility Service
 * Monitors Google Voice package and performs CRUD through content provider
 */
public class GoogleVoiceAccessibilityService extends AccessibilityService {
    public static final String TAG = "GoogleVoiceAccessibilityService";

    private static boolean isInit = false;
    private static int unreadCount;
    private static String text;
    private static List<String> allMessagesList;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Insert to or clear database base on event type
        String packageName = String.valueOf(event.getPackageName());
        if (packageName.equals(getString(R.string.google_voice_package))) {
            int eventType = event.getEventType();
            if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                insert(event);
                setUnreadCount(getCountFromProvider());
                text = getLastMessageFromProvider();
                allMessagesList = getAllMessagesFromProvider();
            } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                deleteAllRows();
                setUnreadCount(getCountFromProvider());
                text = "";
                allMessagesList.clear();
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        if (!isInit) {
            Log.d(TAG, "onServiceConnected");
            isInit = true;
            unreadCount = getCountFromProvider();
            text = getLastMessageFromProvider();
            allMessagesList = getAllMessagesFromProvider();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupted");
        isInit = false;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int count) {
        unreadCount = count;
    }

    public String getSender() {
        // Message sender
        if (!TextUtils.isEmpty(text) && text.contains(":")) {
            return Utils.parseSender(text);
        } else {
            return "";
        }
    }

    public String getBody() {
        // Message body
        if (!TextUtils.isEmpty(text) && text.contains(":")) {
            return Utils.parseBody(text);
        } else {
            return "";
        }
    }

    public List<String> getAllMessages() {
        return allMessagesList;
    }

    private void insert(AccessibilityEvent event) {
        ContentValues values = new ContentValues();
        values.put(MessageSQLiteHelper.COLUMN_MESSAGE, TextUtils.join("", event.getText().toArray()));
        values.put(MessageSQLiteHelper.COLUMN_TIMESTAMP, event.getEventTime());
        getContentResolver().insert(MessageContentProvider.CONTENT_URI, values);
    }

    private int getCountFromProvider() {
        return getAllMessageCursor("DESC").getCount();
    }

    private String getLastMessageFromProvider() {
        Cursor cursor = getAllMessageCursor("DESC");
        String message = "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            message = cursor.getString(cursor.getColumnIndexOrThrow(MessageSQLiteHelper.COLUMN_MESSAGE));
            cursor.close();
        }
        return message;
    }

    private Cursor getAllMessageCursor(String sortOrder) {
        // Return cursor for read all
        return getContentResolver().query(MessageContentProvider.CONTENT_URI,
                new String[] { MessageSQLiteHelper.COLUMN_ID, MessageSQLiteHelper.COLUMN_MESSAGE }, null,
                null, MessageSQLiteHelper.COLUMN_TIMESTAMP + " " + sortOrder);
    }

    private List<String> getAllMessagesFromProvider() {
        List<String> allMessages = new LinkedList<String>();
        Cursor cursor = getAllMessageCursor("DESC");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String message = cursor.getString(cursor.getColumnIndexOrThrow(MessageSQLiteHelper.COLUMN_MESSAGE));
                allMessages.add(message);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return allMessages;
    }

    private void deleteAllRows() {
        // Bulk delete doesn't notify content listener, delete one at a time
        Cursor cursor = getAllMessageCursor("ASC");
        List<Integer> ids = new ArrayList<Integer>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(MessageSQLiteHelper.COLUMN_ID)));
                cursor.moveToNext();
            }
            cursor.close();
        }
        Uri uri;
        for (int id : ids) {
            uri = ContentUris.withAppendedId(MessageContentProvider.CONTENT_URI, id);
            getContentResolver().delete(uri, null, null);
        }
    }
}
