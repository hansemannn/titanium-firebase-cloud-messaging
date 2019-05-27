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

	/**
     * Returns a circle shaped Bitmap of a square or rectangular input Bitmap
     * @param bitmap
     * @return
     */
	public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height){
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        }else{
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width)/2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint(Color.TRANSPARENT);
        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

		return output;
    }
}
