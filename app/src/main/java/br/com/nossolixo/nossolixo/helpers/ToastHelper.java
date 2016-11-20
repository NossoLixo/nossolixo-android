package br.com.nossolixo.nossolixo.helpers;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void show(Context context, int message) {
        show(context, context.getResources().getString(message));
    }
}
