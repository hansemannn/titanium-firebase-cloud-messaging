package firebase.cloudmessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.leolin.shortcutbadger.ShortcutBadger;

public class TiFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    private static final AtomicInteger atomic = new AtomicInteger(0);

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        CloudMessagingModule module = CloudMessagingModule.getInstance();
        if (module != null) {
            module.onTokenRefresh(s);
        }
        Log.d(TAG, "New token: " + s);
    }

    /**
     * Attempts to handle a Firebase Cloud Messaging remote message using the Braze SDK if it's available.
     * This method uses reflection to dynamically call the Braze SDK's handling method without requiring
     * a direct dependency on the Braze SDK.
     * 
     * @param remoteMessage The Firebase RemoteMessage object to be processed
     * @return true if the message was successfully handled by Braze, false otherwise
     */
    @SuppressWarnings({"all"})
    private boolean handleBrazeRemoteMessage(RemoteMessage remoteMessage) {
        Context context = getApplicationContext();

        try {
            return (Boolean) Class.forName("com.braze.push.BrazeFirebaseMessagingService")
                    .getMethod("handleBrazeRemoteMessage", Context.class, RemoteMessage.class)
                    .invoke(null, context, remoteMessage);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        HashMap<String, Object> msg = new HashMap<>();
        CloudMessagingModule module = CloudMessagingModule.getInstance();
        boolean isVisible = true;

        if (handleBrazeRemoteMessage(remoteMessage)) {
            return;
        }

        if (!remoteMessage.getData().isEmpty()) {
            // data message
            isVisible = showNotification(remoteMessage);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            msg.put("title", remoteMessage.getNotification().getTitle());
            msg.put("body", remoteMessage.getNotification().getBody());
            isVisible = true;
        } else {
            Log.d(TAG, "Data message: " + remoteMessage.getData());
        }

        msg.put("from", remoteMessage.getFrom());
        msg.put("ttl", remoteMessage.getTtl());
        msg.put("messageId", remoteMessage.getMessageId());
        msg.put("messageType", remoteMessage.getMessageType());
        msg.put("data", new KrollDict(remoteMessage.getData()));
        msg.put("sendTime", remoteMessage.getSentTime());

        if (isVisible || TiApplication.isCurrentActivityInForeground()) {
            // app is in foreground or notification was show - send data to event receiver
            if (module != null) {
                module.onMessageReceived(msg);
            }
        }
    }

    private Boolean showNotification(RemoteMessage remoteMessage) {
        CloudMessagingModule module = CloudMessagingModule.getInstance();
        Map<String, String> params = remoteMessage.getData();
        JSONObject jsonData = new JSONObject(params);
        boolean showNotification = true;
        Context context = getApplicationContext();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int builder_defaults = 0;
        String parseTitle = "";
        String parseText = "";
        boolean isParse = false;

        if ( TiApplication.isCurrentActivityInForeground()) {
            showNotification = false;
        }

        String forceShowInFg = getString(params, "force_show_in_foreground");
        if (!forceShowInFg.isEmpty()) {
            showNotification = showNotification || TiConvert.toBoolean(forceShowInFg, false);
        }

        if (module != null && module.forceShowInForeground()) {
            showNotification = module.forceShowInForeground();
        }

        if (TiConvert.toBoolean(params.get("vibrate"), false)) {
            builder_defaults |= Notification.DEFAULT_VIBRATE;
        }

        if (params.get("title") == null && params.get("alert") == null && params.get("message") == null
                && params.get("big_text") == null && params.get("big_text_summary") == null && params.get("ticker") == null
                && params.get("image") == null) {
            // no actual content - don't show it
            showNotification = false;
        }

        // Check if it is a default Parse/Sashido message ("data.data.alert")
        String parseData = params.get("data");
        if (parseData != null) {
            try {
                // Parse notification
                JSONObject localJsonData = new JSONObject(parseData);
                parseTitle = localJsonData.get("alert").toString();
                showNotification = true;
                isParse = true;
                parseText = localJsonData.get("text").toString();
            } catch (JSONException e) {
                //
            }
        }

        String priorityValue = getString(params, "priority");
        int priority = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            priority = NotificationManager.IMPORTANCE_DEFAULT;

            if (!priorityValue.isEmpty()) {
                if (priorityValue.equalsIgnoreCase("low")) {
                    priority = NotificationManager.IMPORTANCE_LOW;
                } else if (priorityValue.equalsIgnoreCase("min")) {
                    priority = NotificationManager.IMPORTANCE_MIN;
                } else if (priorityValue.equalsIgnoreCase("max")) {
                    priority = NotificationManager.IMPORTANCE_MAX;
                } else if (priorityValue.equalsIgnoreCase("high")) {
                    priority = NotificationManager.IMPORTANCE_HIGH;
                } else {
                    priority = TiConvert.toInt(priorityValue, 1);
                }
            }
        }

        String soundValue = getString(params, "big_text");
        if (!soundValue.isEmpty()) {
            defaultSoundUri = Utils.getSoundUri(soundValue);
            Log.d(TAG, "showNotification custom sound: " + defaultSoundUri);
        } else {
            builder_defaults |= Notification.DEFAULT_SOUND;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("titanium.firebase.cloudmessaging.message", jsonData.toString());
        editor.apply();

        try {
            // adding normal notification fields to the fcm_data node
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            if (notification != null) {
                String title = notification.getTitle();
                String body = notification.getBody();

                if (title != null && !title.isEmpty()) {
                    jsonData.put("notification_title", title);
                }
                if (body != null && !body.isEmpty()) {
                    jsonData.put("notification_body", body);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error adding fields: " + ex.getMessage());
        }

        if (!showNotification) {
            // hidden notification - still send broadcast with data for next app start
            Intent i = new Intent().setAction("ti.firebase.messaging.hidden-notification");
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("fcm_data", jsonData.toString());
            sendBroadcast(i);
            return false;
        }

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("fcm_data", jsonData.toString());

        int requestCode = (int) (System.currentTimeMillis() / 1000);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, requestCode, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        // Start building notification

        NotificationCompat.Builder builder;
        String channelValue = getString(params, "channelId");
        String channelId = channelValue.isEmpty() ? "default" : channelValue;
        builder = new NotificationCompat.Builder(context, channelId);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        builder.setPriority(priority);
        builder.setContentTitle(params.get("title"));
        if (params.get("alert") != null) {
            // OneSignal uses alert for the message
            builder.setContentText(params.get("alert"));
        } else {
            builder.setContentText(params.get("message"));
        }
        if (isParse) {
            builder.setContentTitle(parseTitle);
            if (!parseText.isEmpty()) {
                builder.setContentText(parseText);
            }
        }

        builder.setTicker(params.get("ticker"));
        builder.setDefaults(builder_defaults);
        builder.setSound(defaultSoundUri);

        // BigText
        String bigTextValue = getString(params, "big_text");
        if (!bigTextValue.isEmpty()) {
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.bigText(bigTextValue);

            String bigTextSummaryValue = getString(params, "big_text_summary");
            if (!bigTextSummaryValue.isEmpty()) {
                bigTextStyle.setSummaryText(bigTextSummaryValue);
            }

            builder.setStyle(bigTextStyle);
        }

        // Icons
        try {
            int smallIcon = this.getResource("notificationicon");
            int smallAppIcon = this.getResource("appicon");
            if (smallIcon > 0) {
                // use custom icon
                builder.setSmallIcon(smallIcon);
            } else if (smallAppIcon > 0) {
                // use app icon
                builder.setSmallIcon(smallAppIcon);
            } else {
                // fallback
                builder.setSmallIcon(android.R.drawable.stat_sys_warning);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Smallicon exception: " + ex.getMessage());
        }

        String colorValue = getString(params, "color");
        if (!colorValue.isEmpty()) {
            try {
                int color = TiConvert.toColor(colorValue, TiApplication.getAppCurrentActivity());
                builder.setColor(color);
                builder.setColorized(true);
            } catch (Exception ex) {
                Log.e(TAG, "Color exception: " + ex.getMessage());
            }
        }

        // Large icon
        String iconValue = getString(params, "icon");
        if (!iconValue.isEmpty()) {
            try {
                Bitmap icon = this.getBitmapFromURL(iconValue);
                //Check if the icon should be displayed as a circle
                if (jsonData.optBoolean("rounded_large_icon")) {
                    //Converting the icon Bitmap to a circle shaped Bitmap
                    icon = Utils.getCircleBitmap(icon);
                }
                builder.setLargeIcon(icon);
            } catch (Exception ex) {
                Log.e(TAG, "Icon exception: " + ex.getMessage());
            }
        }

        // Large icon
        String imageValue = getString(params, "image");
        if (!imageValue.isEmpty()) {
            try {
                Bitmap image = this.getBitmapFromURL(imageValue);
                NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
                notiStyle.bigPicture(image);
                builder.setStyle(notiStyle);
            } catch (Exception ex) {
                Log.e(TAG, "Image exception: " + ex.getMessage());
            }
        }

        // Badge number
        String badgeValue = getString(params, "badge");
        if (!badgeValue.isEmpty()) {
            int badgeNumber = TiConvert.toInt(badgeValue, 1);
            ShortcutBadger.applyCount(context, badgeNumber);
            builder.setNumber(badgeNumber);
        }

        int id = 0;
        String idValue = getString(params, "id");
        if (!idValue.isEmpty()) {
            // ensure that the id sent from the server is negative to prevent
            // collision with the atomic integer
            id = TiConvert.toInt(idValue, 0);
        }

        if (id == 0) {
            id = atomic.getAndIncrement();
        }

        // Send
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
        return true;
    }

    private Bitmap getBitmapFromURL(String src) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) (new URL(src)).openConnection();
        connection.setDoInput(true);
        connection.setUseCaches(false); // Android BUG
        connection.connect();
        return BitmapFactory.decodeStream(new BufferedInputStream(connection.getInputStream()));
    }

    private int getResource(String name) {
        int icon = 0;
        if (name != null) {
            int index = name.lastIndexOf(".");
            if (index > 0)
                name = name.substring(0, index);
            try {
                icon = TiRHelper.getApplicationResource("drawable." + name);
            } catch (TiRHelper.ResourceNotFoundException ex) {
                Log.w(TAG, "drawable." + name + " not found; make sure it's in platform/android/res/drawable");
            }
        }

        return icon;
    }

    private String getString(Map<String, String> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return "";
        }

        return value.toString();
    }
}
