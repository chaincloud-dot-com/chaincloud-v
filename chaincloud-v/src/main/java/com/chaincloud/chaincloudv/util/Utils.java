/**
 * Copyright 2011 Google Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chaincloud.chaincloudv.util;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.chaincloud.chaincloudv.ChainCloudVApplication_;

import java.io.File;
import java.net.URI;

/**
 * A collection of various utility methods that are helpful for working with the Bitcoin protocol.
 * To enable debug logging from the library, run with -Dbitcoinj.logging=true on your command line.
 */
public class Utils {

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean compareString(String str, String other) {
        if (str == null) {
            return other == null;
        } else {
            return other != null && str.equals(other);
        }
    }

    public static File convertUriToFile(Uri uri) {
        File file = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = ChainCloudVApplication_.getInstance().getApplicationContext()
                    .getContentResolver().query
                    (uri, proj, null, null, null);
            if (cursor != null) {
                int actual_image_column_index = cursor.getColumnIndexOrThrow
                        (MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String img_path = cursor.getString(actual_image_column_index);
                if (!Utils.isEmpty(img_path)) {
                    file = new File(img_path);
                }
                cursor.close();
            } else {

                file = new File(new URI(uri.toString()));
                if (file.exists()) {
                    return file;
                }

            }
        } catch (Exception e) {
        }
        return file;
    }

    public static boolean isNubmer(Object obj) {
        try {
            Double.parseDouble(obj.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInteger(Object obj) {
        try {
            Integer.parseInt(obj.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLong(Object obj) {
        try {
            Long.parseLong(obj.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
