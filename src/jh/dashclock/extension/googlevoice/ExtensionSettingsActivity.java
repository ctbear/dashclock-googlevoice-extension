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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

/**
 * Settings activity
 */
public class ExtensionSettingsActivity extends PreferenceActivity {
    public static final String TAG = "ExtensionSettingsActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setIcon(R.drawable.icon);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_config);
        Preference showMessagePreference = findPreference(GoogleVoiceExtension.PREF_SHOW_MESSAGE);
        showMessagePreference.setOnPreferenceChangeListener(prefListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Preference.OnPreferenceChangeListener prefListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference.getKey().equals(GoogleVoiceExtension.PREF_SHOW_MESSAGE)) {
                // Enable/disable other checkboxes
                enableSenderBodyCheckboxes((Boolean) value);
            }

            return true;
        }
    };

    private void enableSenderBodyCheckboxes(boolean enabled) {
        findPreference(GoogleVoiceExtension.PREF_SHOW_SENDER).setEnabled(enabled);
        findPreference(GoogleVoiceExtension.PREF_SHOW_BODY).setEnabled(enabled);
    }
}