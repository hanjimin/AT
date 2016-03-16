package co.favorie.at.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by bmac on 2015-10-30.
        */
public class ATBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            // 부팅이 완료되면 Notification Service 실행
            Intent i = new Intent(context, ATNotificationService.class);
            context.startService(i);
        }
    }
}
