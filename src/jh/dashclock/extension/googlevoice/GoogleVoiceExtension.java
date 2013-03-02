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

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import jh.dashclock.extension.googlevoice.provider.MessageContentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension service for publishing new data
 */
public class GoogleVoiceExtension extends DashClockExtension {
    public static final String TAG = "GoogleVoiceExtension";
    public static final String ACCESSIBILITY_SERVICE_TAG = GoogleVoiceAccessibilityService.TAG;
    public static final String PREF_SHOW_MESSAGE = "pref_show_message";
    public static final String PREF_STACK_MESSAGE = "pref_stack_message";
    public static final String PREF_SHOW_SENDER = "pref_show_sender";
    public static final String PREF_SHOW_BODY = "pref_show_body";
    public static final String PREF_ALWAYS_SHOW = "pref_always_show";

    private static final int MAX_STACKED_MESSAGE_LINES = 3;

    private GoogleVoiceAccessibilityService accessibilityService;

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        if (accessibilityService == null) {
            accessibilityService = new GoogleVoiceAccessibilityService();
        }
        if (!isReconnect) {
            addWatchContentUris(new String[] { MessageContentProvider.CONTENT_URI.toString() });
        }
        setUpdateWhenScreenOn(true);
    }

    @Override
    public void onDestroy() {
        if (accessibilityService != null) {
            accessibilityService.onInterrupt();
        }
    }

    @Override
    protected void onUpdateData(int reason) {
        if (!accessibilityServiceOn()) {
            Log.w(TAG, "Accessibility service is not on.");
            return;
        }

        int unreadCount = accessibilityService.getUnreadCount();
        final Intent googleVoiceIntent = getGoogleVoiceIntent();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showMessage = sp.getBoolean(PREF_SHOW_MESSAGE, true);
        boolean stackMessage = sp.getBoolean(PREF_STACK_MESSAGE, false);
        boolean showSender = sp.getBoolean(PREF_SHOW_SENDER, true);
        boolean showBody = sp.getBoolean(PREF_SHOW_BODY, true);
        boolean alwaysShow = sp.getBoolean(PREF_ALWAYS_SHOW, false);

        String sender = "";
        String body = "";
        // Default message body
        if (unreadCount == 1) {
            body = "1 Unread Message";
        } else {
            body = unreadCount + " Unread Messages";
        }
        if (showMessage && unreadCount > 0) {
            sender = processSender(stackMessage, showSender);
            if (showBody && !stackMessage) {
                body = accessibilityService.getBody();
            }
        }
        // Publish the extension data update.
        ExtensionData data = new ExtensionData()
                .visible(unreadCount != 0 || alwaysShow)
                .icon(R.drawable.google_voice)
                .status("" + unreadCount)
                .expandedTitle(body)
                .expandedBody(sender)
                .clickIntent(googleVoiceIntent);
        data.clean();
        publishUpdate(data);
    }

    private Intent getGoogleVoiceIntent() {
        // Intent to launch Google Voice app
        String googleVoicePackage = getString(R.string.google_voice_package);
        String activity = ".SplashActivity";
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(ComponentName.unflattenFromString(googleVoicePackage + "/"
                + googleVoicePackage + activity));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return intent;
    }

    private boolean accessibilityServiceOn() {
        AccessibilityManager manager = (AccessibilityManager) getApplicationContext()
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfoList = manager.getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo serviceInfo : serviceInfoList) {
            if (serviceInfo.getId().contains(ACCESSIBILITY_SERVICE_TAG)) {
                return true;
            }
        }
        return false;
    }

    private String processSender(boolean stackMessage, boolean showSender) {
        // Process expanded body based on options
        if (stackMessage) {
            List<String> messages = new ArrayList<String>();
            List<String> allMessages = accessibilityService.getAllMessages();
            int width = Utils.getDisplaySize(getBaseContext()).x;
            for (String message : allMessages) {
//                    messages.add(Utils.ellipsizeString(message, width));
                messages.add(TextUtils.ellipsize(message, new TextPaint(), width / 3.5f,
                        TextUtils.TruncateAt.END) + "");
                if (messages.size() >= MAX_STACKED_MESSAGE_LINES
                        && allMessages.size() > MAX_STACKED_MESSAGE_LINES) {
                    messages.add("...");
                    break;
                }
            }
            return TextUtils.join("\n", messages);
        } else if (showSender) {
            return "Last from: " + accessibilityService.getSender();
        } else {
            return "";
        }
    }
}

