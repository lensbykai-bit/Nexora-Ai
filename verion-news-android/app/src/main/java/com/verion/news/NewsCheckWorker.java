package com.verion.news;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NewsCheckWorker extends Worker {
    public static final String CHANNEL_ID = "verion_latest_news";
    private static final String FEED = "https://verionnewss.blogspot.com/feeds/posts/default?alt=json&max-results=1";
    private static final String HOME = "https://verionnewss.blogspot.com/";
    private static final String PREFS = "verion_news_prefs";
    private static final String LAST_ID = "last_post_id";

    public NewsCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    @NonNull @Override public Result doWork() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(FEED).openConnection();
            connection.setConnectTimeout(12000); connection.setReadTimeout(12000); connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json"); connection.setRequestProperty("User-Agent", "VERIONNEWS-Android/1.7.0");
            int responseCode = connection.getResponseCode(); if (responseCode < 200 || responseCode >= 300) return Result.retry();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder json = new StringBuilder(); String line; while ((line = reader.readLine()) != null) json.append(line); reader.close();
            JSONObject root = new JSONObject(json.toString()); JSONArray entries = root.getJSONObject("feed").optJSONArray("entry");
            if (entries == null || entries.length() == 0) return Result.success();
            JSONObject post = entries.getJSONObject(0);
            String id = post.optJSONObject("id") != null ? post.getJSONObject("id").optString("$t", "") : "";
            String title = post.optJSONObject("title") != null ? post.getJSONObject("title").optString("$t", "New story from VERION NEWS") : "New story from VERION NEWS";
            String link = findAlternateLink(post.optJSONArray("link"));
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE); String previous = prefs.getString(LAST_ID, "");
            if (id.isEmpty()) return Result.success();
            if (previous.isEmpty()) { prefs.edit().putString(LAST_ID, id).apply(); return Result.success(); }
            if (!id.equals(previous)) { prefs.edit().putString(LAST_ID, id).apply(); showNotification(title, link); }
            return Result.success();
        } catch (Exception e) { return Result.retry(); } finally { if (connection != null) connection.disconnect(); }
    }

    private String findAlternateLink(JSONArray links) {
        if (links == null) return HOME;
        for (int i=0;i<links.length();i++) { JSONObject item=links.optJSONObject(i); if(item!=null&&"alternate".equals(item.optString("rel"))){String href=item.optString("href","");if(href.startsWith(HOME))return href;} }
        return HOME;
    }

    private void showNotification(String title, String url) {
        Context context=getApplicationContext(); createChannel(context);
        if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        Intent intent=new Intent(context,MainActivity.class); intent.putExtra("article_url",url); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,(int)(System.currentTimeMillis()&0x0fffffff),intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("VERION NEWS").setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(title)).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL).setAutoCancel(true).setOnlyAlertOnce(true).setContentIntent(pendingIntent);
        NotificationManagerCompat.from(context).notify((int)(System.currentTimeMillis()&0x0fffffff),builder.build());
    }

    public static void createChannel(Context context) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Latest VERION NEWS",NotificationManager.IMPORTANCE_HIGH);channel.setDescription("Notifications when VERION NEWS publishes a new story");channel.enableVibration(true);NotificationManager manager=context.getSystemService(NotificationManager.class);if(manager!=null)manager.createNotificationChannel(channel);}
    }
}
