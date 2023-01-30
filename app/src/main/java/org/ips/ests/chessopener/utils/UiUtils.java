package org.ips.ests.chessopener.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.ips.ests.chessopener.R;

public class UiUtils {

	public static void doToast(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}

    public static void showAboutDialog(Context context) {
		AlertDialog.Builder builder =
				new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
		builder.setTitle("Chess Opener " + getVersion(context));
		builder.setMessage(Html.fromHtml(context.getString(R.string.description)));
		builder.setPositiveButton("OK", null);
		builder.show();
	}

    public static String getVersion(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
