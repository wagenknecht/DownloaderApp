package de.wagenknecht.downloaderapp;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends Service {

    private String getUrl;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String getUrl = intent.getStringExtra("url");
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFile(getUrl);
            }
        }).start();

        final String CHANNELID = "Foreground";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification= new Notification.Builder(this, CHANNELID)
                .setContentText("Download still running")
                .setSmallIcon(R.drawable.ic_launcher_background);
        startForeground(101, notification.build());

        return super.onStartCommand(intent,flags,startId);
    }


    @Nullable
    @Override
    public IBinder onBind (Intent intent){
        return null;
    }


    public void downloadFile(String startUrl) {
        try {
            Log.d(TAG, "downloadFile: " + startUrl);
            URL fileurl = new URL(startUrl);
            String fileName = startUrl.substring( startUrl.lastIndexOf('/')+1, startUrl.length() );
            URLConnection urlConnection = fileurl.openConnection();
            urlConnection.connect();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream(),8192);

            File downloadordner = new File(Environment.getExternalStorageDirectory(), "Download");
            if(!downloadordner.exists()){
                downloadordner.mkdirs();
            }

            File downloadedFile = new File(downloadordner, fileName);
            OutputStream outputStream = new FileOutputStream(downloadedFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
