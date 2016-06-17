package com.brtbeacon.indoor.util;

import android.os.Build;

/**
 * Created by Brightbeacon on 2016/6/7 0007.
 */
public class VersionUtil {

    /**
     * 判断是否为android6.0或以上的版本
     * @return
     */
    public static boolean isV23Plus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }
        return false;
    }
}
