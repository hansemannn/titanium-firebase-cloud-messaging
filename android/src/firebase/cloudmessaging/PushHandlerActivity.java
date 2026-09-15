package firebase.cloudmessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity {

    private static final String LCAT = "FirebaseCloudMessaging";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            // Must come before startActivity(). When the app is not running, this activity's task
            // is the app's only one, and the launcher intent would join it and be removed with it.
            finish();

            CloudMessagingModule module = CloudMessagingModule.getInstance();
            Context context = getApplicationContext();
            String notification = getIntent().getStringExtra("fcm_data");

            // The payload travels by exactly one route. If a listener took it, the
            // Intent must not carry it as well: Titanium resumes the root activity
            // twice, and the second pass would deliver the same notification again.
            boolean delivered = (module != null) && module.setNotificationData(notification);

            Intent launcherIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            assert launcherIntent != null;
            // Titanium's TiRootActivity only recognises its own main intent. Anything
            // else makes it spawn a second root activity, which shows an extra splash
            // before finishing itself. Matching that intent keeps the launch to one.
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launcherIntent.setPackage(context.getPackageName());
            if (!delivered) {
                launcherIntent.putExtra("fcm_data", notification);
            }

            startActivity(launcherIntent);

        } catch (Exception e) {
            // noop
        } finally {
            finish();
        }
    }

    @Override
    protected void onResume() {
        Log.d(LCAT, "resumed");
        super.onResume();
    }
}
