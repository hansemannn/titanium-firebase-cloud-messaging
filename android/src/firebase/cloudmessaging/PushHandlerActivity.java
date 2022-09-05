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
            finish();

            CloudMessagingModule module = CloudMessagingModule.getInstance();
            Context context = getApplicationContext();
            String notification = getIntent().getStringExtra("fcm_data");

            if (module != null) {
                module.setNotificationData(notification);
            }

            Intent launcherIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            assert launcherIntent != null;
            launcherIntent.addCategory(Intent.ACTION_MAIN);
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launcherIntent.putExtra("fcm_data", notification);

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
