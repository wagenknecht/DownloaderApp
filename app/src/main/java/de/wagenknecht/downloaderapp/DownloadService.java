package de.wagenknecht.downloaderapp;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends Service {
    public 	static final int 	UPDATE_PROGRESS
            = 8344;
    public 	static final String URL
            = "url";
    public 	static final String RECEIVER
            = "receiver";
    private static final int	BUFFERSIZE
            = 1024;
    private static final String STORAGENOTWRITABLE
            = "external storage is not accessable or not writable";


    private String getUrl;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String getUrl = intent.getStringExtra("url");
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFile(getUrl, intent);
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


    public void downloadFile(String startUrl, Intent intent) {
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        try {
            Log.d(TAG, "downloadFile: " + startUrl);
            URL fileurl = new URL(startUrl);
            String fileName = startUrl.substring( startUrl.lastIndexOf('/')+1, startUrl.length() );
            URLConnection urlConnection = fileurl.openConnection();
            urlConnection.connect();
            int fileLength = urlConnection.getContentLength();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream(),8192);

            File downloadordner = new File(Environment.getExternalStorageDirectory(), "Download");
            if(!downloadordner.exists()){
                downloadordner.mkdirs();
            }

            File downloadedFile = new File(downloadordner, fileName);
            OutputStream outputStream = new FileOutputStream(downloadedFile);

            byte[] buffer = new byte[1024];
            long total = 0;
            int read;
            while ((read = inputStream.read(buffer)) != -1){
                total += read;

                Bundle resultData = new Bundle();
                //Probleme mit Maximaler Zahl von Integer in Java
                resultData.putInt(DownloadReceiver.PROGRESS, (int) ((long) total * 100 / fileLength));
                Log.d(TAG, "downloadFile: " + (long) total * 100 / fileLength + " total:" + total + " filelength:" + fileLength);
                receiver.send(UPDATE_PROGRESS, resultData);
                outputStream.write(buffer, 0, read);

            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            final String CHANNELID = "Foreground";
            NotificationChannel channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification= new Notification.Builder(this, CHANNELID)
                    .setContentText("Fertig")
                    .setSmallIcon(R.drawable.ic_launcher_background);
            startForeground(101, notification.build());

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
