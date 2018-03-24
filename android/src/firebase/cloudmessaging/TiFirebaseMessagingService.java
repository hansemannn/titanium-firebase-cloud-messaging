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

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TiFirebaseMessagingService extends FirebaseMessagingService {

	private static final String TAG = "FirebaseMsgService";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.i(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.i(TAG, "Message data payload: " + remoteMessage.getData());

			if (/* Check if data needs to be processed by long running job */ true) {
				// For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
				scheduleJob();
			} else {
				// Handle message within 10 seconds
				handleNow();
			}

		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.i(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
		}
	}

	/**
	* Schedule a job using FirebaseJobDispatcher.
	*/
	private void scheduleJob() {
		// [START dispatch_job]
		FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
		Job myJob = dispatcher.newJobBuilder()
			.setService(TiJobService.class)
			.setTag("my-job-tag")
			.build();
		dispatcher.schedule(myJob);
		// [END dispatch_job]
	}

	/**
	* Handle time allotted to BroadcastReceivers.
	*/
	private void handleNow() {
		Log.i(TAG, "Short lived task is done.");
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
