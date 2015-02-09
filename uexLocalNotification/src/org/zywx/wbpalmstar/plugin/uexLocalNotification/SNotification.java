package org.zywx.wbpalmstar.plugin.uexLocalNotification;

import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class SNotification {

	public static int mId;

	@SuppressWarnings("deprecation")
	public static void notification(Context context, Alerm alerm) {
		Log.e("==notification==", "===notification=start=");
		Intent notyIntent = new Intent(context, EBrowserActivity.class);
		notyIntent.putExtra(Const.KEY_DATA, alerm);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		int iconId = EUExUtil.getResDrawableID("icon");
		Notification notification = new Notification(iconId, alerm.title,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		String ringName = alerm.mode;
		if (null != ringName && 0 != ringName.length()) {
			if (ringName.equals("system")) {
				notification.sound = Uri.withAppendedPath(
						Audio.Media.INTERNAL_CONTENT_URI, "6"); // 系统铃声
			} else if (ringName.equals("default")) {
				notification.defaults |= Notification.DEFAULT_SOUND; // 默认铃声
			}
		}
		notification.setLatestEventInfo(context, alerm.title, alerm.content,
				contentIntent);
		NotificationManager mMgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mMgr.notify(mId, notification);
		EUexLocalNotify.addToMap(alerm.notifyId, mId);
		Log.e("==notification==", "===notification=end=");
	}
}