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

public class TiFirebaseMessagingService extends FirebaseMessagingService {

	private static final String TAG = "FirebaseMsgService";

	@Override
	public void onMessageSent(String msgID) {
		Log.d(TAG, "Message sent: " + msgID );
		//super.onMessageSent(msgID);
	}

	@Override
	public void onSendError(String msgID, Exception exception) {
		Log.d(TAG, "Sent Error : " + msgID + " " + exception);
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

			HashMap<String, Object> msg = new HashMap<String, Object>();
			msg.put("from", remoteMessage.getFrom());
			msg.put("title", remoteMessage.getNotification().getTitle());
			msg.put("body", remoteMessage.getNotification().getBody());
			CloudMessagingModule.getInstance().onMessageReceived(msg);
		}
	}

	/**
	* Create and show a simple notification containing the received FCM message.
	*
	* @param messageBody FCM message body received.
	*/
	private void sendNotification(String messageBody) {
		Intent intent = new Intent(this, CloudMessagingModule.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
		PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Context context = getApplicationContext();
		NotificationCompat.Builder notificationBuilder =
		new NotificationCompat.Builder(context)
			// .setSmallIcon(R.drawable.ic_stat_ic_notification)
			.setContentTitle("FCM Message")
			.setContentText(messageBody)
			.setAutoCancel(true)
			.setSound(defaultSoundUri)
			.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
		(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Since android Oreo notification channel is needed.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("tiFirebase",
			"Channel human readable title",
			NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		}

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}
}
