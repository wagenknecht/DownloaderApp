package de.wagenknecht.downloaderapp;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(DownloadService.this,"alarmChannel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Download Status")
                .setContentText("Dein Download läuft.")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(DownloadService.this);
        notificationManagerCompat.notify(1, builder.build());
        startForeground(1, builder.build());


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

            stopForeground(true);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(DownloadService.this,"alarmChannel")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Download Status")
                    .setContentText("Dein Download ist abgeschlossen.")
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(DownloadService.this);
            notificationManagerCompat.notify(1, builder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
