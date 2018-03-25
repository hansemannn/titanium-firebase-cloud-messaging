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
		Log.d(TAG, "Message sent: " + msgID);
	}

	@Override
	public void onSendError(String msgID, Exception exception) {
		Log.d(TAG, "Sent Error: " + msgID + " " + exception);
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Log.d(TAG, "From: " + remoteMessage.getFrom());

		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

			HashMap<String, Object> msg = new HashMap<String, Object>();
			msg.put("from", remoteMessage.getFrom());
			msg.put("title", remoteMessage.getNotification().getTitle());
			msg.put("body", remoteMessage.getNotification().getBody());
			CloudMessagingModule.getInstance().onMessageReceived(msg);
		}
	}
}
