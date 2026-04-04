package com.yourcompany.iptv.receiver; // 替换成你的包名

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.yourcompany.iptv.MainActivity; // 替换成你的包名

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        }
    }
}
