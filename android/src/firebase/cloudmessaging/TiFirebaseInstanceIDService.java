package firebase.cloudmessaging;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class TiFirebaseInstanceIDService extends FirebaseInstanceIdService {

	private static final String TAG = "TiFirebaseIIDService";

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. Note that this is called when the InstanceID token
	 * is initially generated so this is where you would retrieve the token.
	 */
	@Override
	public void onTokenRefresh() {
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.i(TAG, "Refreshed token: " + refreshedToken);
		//sendRegistrationToServer(refreshedToken);
	}

	// private void sendRegistrationToServer(String token) {
	// 	// TODO: Implement this method to send token to your app server.
	// }
}
