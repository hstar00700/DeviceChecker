package kr.hstar.devicechecker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.orhanobut.logger.Logger;

public class OnInstallReceiver extends BroadcastReceiver {
    private static final String TAG = OnInstallReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.w("OnInstallReceiver received!!!");
        Intent i=new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            Logger.w("update app!!!");
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Logger.e("Error : " + e.getLocalizedMessage());
        }
    }
}
