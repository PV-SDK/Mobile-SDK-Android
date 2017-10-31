package com.powervision.powersdk.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.powervision.powersdk.R;


/**
 * Created by Andrea.Cao on 2017/5/6.
 */

public class StandardDialogUtils {

    private StandardDialogUtils() {

    }

    public static void defaultDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.waring)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(R.string.text_sure, null)
                .create()
                .show();
    }

}
