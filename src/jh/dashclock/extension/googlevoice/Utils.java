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

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Utilities
 */
public class Utils {

    public static String parseSender(String text) {
        return text.split(":", 2)[0].trim();
    }

    public static String parseBody(String text) {
        return text.split(":", 2)[1].trim();
    }

    public static String ellipsizeString(String string, int truncateAt) {
        if (string.length() >  truncateAt) {
            String copy = string.substring(0, truncateAt - 3);
            return copy + "...";
        } else {
            return string;
        }
    }

    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}
