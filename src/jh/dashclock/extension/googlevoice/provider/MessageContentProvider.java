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

package jh.dashclock.extension.googlevoice.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import jh.dashclock.extension.googlevoice.storage.MessageSQLiteHelper;

/**
 * Content provider for accessibility service
 */
public class MessageContentProvider extends ContentProvider {
    public static final String TAG = "MessageContentProvider";

    private static final int MESSAGES = 98;
    private static final int MESSAGE_ID = 99;
    private static final String AUTHORITY = "jh.dashclock.extension.googlevoice.provider";
    private static final String BASE_PATH = "messages";
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, MESSAGES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MESSAGE_ID);
    }

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static MessageSQLiteHelper database;

    @Override
    public boolean onCreate() {
        database = new MessageSQLiteHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MessageSQLiteHelper.TABLE_NAME);

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case MESSAGES:
                break;
            case MESSAGE_ID:
                queryBuilder.appendWhere(MessageSQLiteHelper.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor =  queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case MESSAGES:
                id = sqlDB.insert(MessageSQLiteHelper.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case MESSAGES:
                rowsDeleted = sqlDB.delete(MessageSQLiteHelper.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case MESSAGE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(MessageSQLiteHelper.TABLE_NAME,
                            MessageSQLiteHelper.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(MessageSQLiteHelper.TABLE_NAME,
                            MessageSQLiteHelper.COLUMN_ID + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Currently not needed, return 0
        return 0;
    }
}
