package br.com.nossolixo.nossolixo.helpers;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogHelper {
    private ProgressDialog mProgressDialog;

    public ProgressDialogHelper(Context context, String message) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(message);
    }

    public void show() {
        mProgressDialog.show();
    }

    public boolean isShowing() {
        return mProgressDialog.isShowing();
    }

    public void hide() {
        mProgressDialog.dismiss();
    }
}
