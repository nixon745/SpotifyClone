package com.example.spotifyclone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // שינוי התנאי לסוללה חלשה
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            Toast.makeText(context, "אזהרה: סוללה חלשה! כדאי לחבר מטען", Toast.LENGTH_LONG).show();

            // בונוס: עצירת המוזיקה אם היא מנגנת
            MusicManager.getInstance().stopMusic();
        }
    }
}