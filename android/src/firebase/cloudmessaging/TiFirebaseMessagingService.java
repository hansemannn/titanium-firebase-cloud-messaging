package firebase.cloudmessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.HashMap;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.appcelerator.kroll.KrollDict;
import java.util.concurrent.atomic.AtomicInteger;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.Random;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedInputStream;
import org.appcelerator.titanium.util.TiRHelper;
import java.util.Map;
import org.json.JSONObject;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.TiApplication;

public class TiFirebaseMessagingService extends FirebaseMessagingService
{

	private static final String TAG = "FirebaseMsgService";
	private static final AtomicInteger atomic = new AtomicInteger(0);

	@Override
	public void onMessageSent(String msgID)
	{
		Log.d(TAG, "Message sent: " + msgID);
	}

	@Override
	public void onSendError(String msgID, Exception exception)
	{
		Log.e(TAG, "Send Error: " + msgID + " " + exception);
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		HashMap<String, Object> msg = new HashMap<String, Object>();
		CloudMessagingModule module = CloudMessagingModule.getInstance();
		Boolean appInForeground = TiApplication.isCurrentActivityInForeground();
		Boolean isVisibile = true;

		if (remoteMessage.getData().size() > 0) {
			// data message
			isVisibile = showNotification(remoteMessage);
		}

		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
			msg.put("title", remoteMessage.getNotification().getTitle());
			msg.put("body", remoteMessage.getNotification().getBody());
			isVisibile = true;
		} else {
			Log.d(TAG, "Data message: " + remoteMessage.getData());
		}

		msg.put("from", remoteMessage.getFrom());
		msg.put("to", remoteMessage.getTo());
		msg.put("ttl", remoteMessage.getTtl());
		msg.put("messageId", remoteMessage.getMessageId());
		msg.put("messageType", remoteMessage.getMessageType());
		msg.put("data", new KrollDict(remoteMessage.getData()));
		msg.put("sendTime", remoteMessage.getSentTime());

		if (isVisibile || appInForeground) {
			// app is in foreground or notification was show - send data to event receiver
			module.onMessageReceived(msg);
		}
	}

	private Boolean showNotification(RemoteMessage remoteMessage)
	{
		Map<String, String> params = remoteMessage.getData();
		JSONObject jsonData = new JSONObject(params);
		Boolean appInForeground = TiApplication.isCurrentActivityInForeground();
		Boolean showNotification = true;
		Context context = getApplicationContext();

		if (appInForeground) {
			showNotification = false;
		}

		if (params.get("force_show_in_foreground") != null && params.get("force_show_in_foreground") != "") {
			showNotification = TiConvert.toBoolean(params.get("force_show_in_foreground"), false);
		}

		if (params.get("title") == null && params.get("message") == null && params.get("big_text") == null
			&& params.get("big_text_summary") == null && params.get("ticker") == null && params.get("image") == null) {
			// no actual content - don't show it
			showNotification = false;
		}

		if (!showNotification) {
			// hidden notification - still send broadcast with data for next app start
			Intent i = new Intent();
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra("fcm_data", jsonData.toString());
			sendBroadcast(i);

			return false;
		}

		Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notificationIntent.putExtra("fcm_data", jsonData.toString());

		PendingIntent contentIntent =
			PendingIntent.getActivity(this, new Random().nextInt(), notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		// Start building notification

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		int builder_defaults = 0;
		builder.setContentIntent(contentIntent);
		builder.setAutoCancel(true);
		builder.setPriority(2);
		builder.setContentTitle(params.get("title"));
		builder.setContentText(params.get("message"));
		builder.setTicker(params.get("ticker"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (params.get("channelId") != null && params.get("channelId") != "") {
				builder.setChannelId(params.get("channelId"));
			} else {
				builder.setChannelId("default");
			}
		}

		// BigText
		if (params.get("big_text") != null && params.get("big_text") != "") {
			NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
			bigTextStyle.bigText(params.get("big_text"));

			if (params.get("big_text_summary") != null && params.get("big_text_summary") != "") {
				bigTextStyle.setSummaryText(params.get("big_text_summary"));
			}

			builder.setStyle(bigTextStyle);
		}

		// Icons
		try {
			int smallIcon = this.getResource("drawable", "notificationicon");
			if (smallIcon > 0) {
				builder.setSmallIcon(smallIcon);
			} else {
				builder.setSmallIcon(android.R.drawable.stat_sys_warning);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Smallicon exception: " + ex.getMessage());
		}

		if (params.get("color") != null && params.get("color") != "") {
			try {
				int color = TiConvert.toColor(params.get("color"));
				builder.setColor(color);
				builder.setColorized(true);
			} catch (Exception ex) {
				Log.e(TAG, "Color exception: " + ex.getMessage());
			}
		}

		// Large icon
		if (params.get("icon") != null && params.get("icon") != "") {
			try {
				Bitmap icon = this.getBitmapFromURL(params.get("icon"));
				builder.setLargeIcon(icon);
			} catch (Exception ex) {
				Log.e(TAG, "Icon exception: " + ex.getMessage());
			}
		}

		// Large icon
		if (params.get("image") != null && params.get("image") != "") {
			try {
				Bitmap image = this.getBitmapFromURL(params.get("image"));
				NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
				notiStyle.bigPicture(image);
				builder.setStyle(notiStyle);
			} catch (Exception ex) {
				Log.e(TAG, "Image exception: " + ex.getMessage());
			}
		}

		int id = 0;
		if (params != null && params.get("id") != "") {
			// ensure that the id sent from the server is negative to prevent
			// collision with the atomic integer
			id = TiConvert.toInt(params.get("id"), 0);
		}

		if (id == 0) {
			id = atomic.getAndIncrement();
		}

		// Send
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(null, id, builder.build());
		return true;
	}

	private Bitmap getBitmapFromURL(String src) throws Exception
	{
		HttpURLConnection connection = (HttpURLConnection) (new URL(src)).openConnection();
		connection.setDoInput(true);
		connection.setUseCaches(false); // Android BUG
		connection.connect();
		return BitmapFactory.decodeStream(new BufferedInputStream(connection.getInputStream()));
	}

	private int getResource(String type, String name)
	{
		int icon = 0;
		if (name != null) {
			int index = name.lastIndexOf(".");
			if (index > 0)
				name = name.substring(0, index);
			try {
				icon = TiRHelper.getApplicationResource(type + "." + name);
			} catch (TiRHelper.ResourceNotFoundException ex) {
				Log.e(TAG, type + "." + name + " not found; make sure it's in platform/android/res/" + type);
			}
		}

		return icon;
	}
}
