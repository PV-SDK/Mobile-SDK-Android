package com.powervision.powersdk.utils;

import android.text.TextUtils;
import android.widget.Toast;

import com.powervision.powersdk.BaseApplication;

public class ToastUtil {

    private static Toast mToast;

    /**
     * 重复执行，不会连续出现的short_toast
     * @param msg
     */
    public static void showToast(CharSequence msg) {

        if (TextUtils.isEmpty(msg)) {
            return;
        }

        if (mToast == null) {
            mToast = Toast.makeText(BaseApplication.getInstance(), msg, Toast.LENGTH_SHORT);
        }

        mToast.show();
    }
}
