package org.zywx.wbpalmstar.plugin.uexLocalNotification;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

public class EUexLocalNotify extends EUExBase {
	private static final String TAG="jsonData";
	private NotificationManager notificationManager;
	private static Map<String, Integer> map = new HashMap<String, Integer>();
	
	public EUexLocalNotify(Context context, EBrowserView inParent) {
		super(context, inParent);
		notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public static void addToMap(String notifyId, int id) {
		map.put(notifyId, id);
	}

	public void add(String[] parm){
		if(parm.length < 8){
			return;
		}
		try{
			String nId = parm[0];
			String time = parm[1];
			String msg = parm[3];
			String mode= parm[5];
			Log.i(TAG, "mode="+mode);
			String interval = parm[6];
			Log.i(TAG, "interval="+interval);
			int appNameId = EUExUtil.getResStringID("app_name");
			String title = mContext.getString(appNameId);
			long startTime = Long.parseLong(time);
			
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(startTime);
			int h = c.get(Calendar.HOUR_OF_DAY);
			int m = c.get(Calendar.MINUTE);
			int month = c.get(Calendar.MONTH);
			int day_of_month = c.get(Calendar.DAY_OF_MONTH);
			int day_of_week = c.get(Calendar.DAY_OF_WEEK);
			int day_of_year = c.get(Calendar.DAY_OF_YEAR);
			Alerm alerm = new Alerm();
			alerm.mode=mode;
			alerm.title = title;
			alerm.content = msg;
			alerm.notifyId = nId;
			alerm.minute = m;
			alerm.hour = h;
			alerm.month = month;
			alerm.start_time = startTime;
			alerm.day_of_month = day_of_month;
			alerm.day_of_week = day_of_week;
			alerm.day_of_year = day_of_year;
			alerm.interval = interval;
			EAlarmReceiver.addAlerm(mContext, alerm);
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void remove(String[] parm){
		String nId = parm[0];
		EAlarmReceiver.cancelAlerm(mContext, nId+mContext.getPackageName());
		if(map != null && map.containsKey(nId)) {
			int id = map.get(nId);
			map.remove(nId);
			if(notificationManager != null) {
				notificationManager.cancel(id);
			}
		}
	}
	
	public void removeAll(String[] parm){
		EAlarmReceiver.cancelAlermAll(mContext);
		if(notificationManager != null) {
			notificationManager.cancelAll();
			if(map != null) {
				map.clear();
			}
		}
	}
	
	@Override
	protected boolean clean() {
		if(map != null) {
			map.clear();
		}
		return false;
	}
}