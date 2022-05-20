package de.wagenknecht.downloaderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

class DownloadReceiver extends ResultReceiver {
    public static final String PROGRESS
            = "progress";
    public static final int	FINISHED
            = 100;

    public DownloadReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == DownloadService.UPDATE_PROGRESS) {
            int progress = resultData.getInt(PROGRESS);

            MainActivity.progressBar.setProgress(progress);
        }
    }
}