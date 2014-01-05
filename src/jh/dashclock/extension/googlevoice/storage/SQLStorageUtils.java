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

package jh.dashclock.extension.googlevoice.storage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import jh.dashclock.extension.googlevoice.provider.MessageContentProvider;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides utility methods for accessing SQL storage
 */
public class SQLStorageUtils {

    public static void insert(ContentResolver resolver, ContentValues values) {
        resolver.insert(MessageContentProvider.CONTENT_URI, values);
    }

    public static int getCountFromProvider(ContentResolver resolver) {
        Cursor cursor = getAllMessageCursor(resolver, "DESC");
        return (cursor == null) ? 0 : cursor.getCount();
    }

    public static String getLastMessageFromProvider(ContentResolver resolver) {
        Cursor cursor = getAllMessageCursor(resolver, "DESC");
        String message = "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            message = cursor.getString(cursor.getColumnIndexOrThrow(MessageSQLiteHelper.COLUMN_MESSAGE));
            cursor.close();
        }
        return message;
    }

    public static Cursor getAllMessageCursor(ContentResolver resolver, String sortOrder) {
        // Return cursor for read all
        return resolver.query(MessageContentProvider.CONTENT_URI,
                new String[] { MessageSQLiteHelper.COLUMN_ID,
                        MessageSQLiteHelper.COLUMN_MESSAGE,
                        MessageSQLiteHelper.COLUMN_TIMESTAMP
                },
                null,
                null, MessageSQLiteHelper.COLUMN_ID + " " + sortOrder);
    }

    public static List<String> getAllMessagesFromProvider(ContentResolver resolver) {
        List<String> allMessages = new LinkedList<String>();
        Cursor cursor = getAllMessageCursor(resolver, "DESC");
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

    public static int delete(ContentResolver resolver, String whereClause) {
        return resolver.delete(MessageContentProvider.CONTENT_URI, whereClause, null);
    }

    public static void deleteAllRows(ContentResolver resolver) {
        // Bulk delete doesn't notify content listener, delete one at a time
        Cursor cursor = getAllMessageCursor(resolver, "ASC");
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
            resolver.delete(uri, null, null);
        }
    }
}
