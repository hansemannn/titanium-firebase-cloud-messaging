package firebase.cloudmessaging;

import android.content.Context;
import org.appcelerator.titanium.TiApplication;

public final class Utils
{

	/**
     * Returns the identifier of the resource at the path res/<type>/<name>
     * @param type
     * @param name
     * @return
     */
	public static int getResourceIdentifier(String type, String name)
	{
		int icon = 0;
		if (name != null) {
			int index = name.lastIndexOf(".");
			if (index > 0) {
				name = name.substring(0, index);
			}
			Context context = Utils.getApplicationContext();
			icon = context.getResources().getIdentifier(name, type, context.getPackageName());
		}

		return icon;
	}

	public static Context getApplicationContext()
	{
		return TiApplication.getInstance().getApplicationContext();
	}
}