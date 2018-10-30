package firebase.cloudmessaging;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class TiFirebaseInstanceIDService extends FirebaseInstanceIdService
{

	private static final String TAG = "TiFirebaseIIDService";

	@Override
	public void onTokenRefresh()
	{
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed token: " + refreshedToken);
		if (CloudMessagingModule.getInstance() != null) {
			CloudMessagingModule.getInstance().onTokenRefresh(refreshedToken);
		}
	}
}
