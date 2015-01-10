package org.zywx.wbpalmstar.plugin.uexLocalNotification;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class EAlarmReceiver extends BroadcastReceiver {

	public static final Hashtable<String, Integer> INTERVAL;
	public static final Hashtable<String, String> ALARM_ACTIONS;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(null != intent){
			String action = intent.getAction();
			Log.i("action", "action="+action);
			if(ALARM_ACTIONS.contains(action)){
				CPUWakeLock.acquireCpuWakeLock(context);
				Alerm alerm = (Alerm) intent.getSerializableExtra(Const.KEY_DATA);
				SNotification.notification(context, alerm);
				CPUWakeLock.releaseCpuLock();
			}else if(Const.BC_ACTION.equals(action) 
					|| Const.TC_ACTION.equals(action)
					|| Const.TZ_ACTION.equals(action)
					|| Const.LC_ACTION.equals(action)){
				initAlerm(context);
			}
		}
	}

	public static void enableNextAlert(Context context, Alerm oldAlerm) {

		setAlerm(context, oldAlerm);
	}
	
	public static void initAlerm(Context context){
		SharedPreferences sp = context.getSharedPreferences(Const.ALARM_SP, Context.MODE_PRIVATE);
		for(Map.Entry<String, String> entry : ALARM_ACTIONS.entrySet()) {
			String ac = entry.getValue();
			String al = sp.getString(ac, null);
			if(null != al && 0 != al.trim().length()){
				Alerm alerm = Alerm.parserJson(al);
				if(null != alerm){
					cancelAlerm(context, alerm.notifyId);
					addAlerm(context, alerm);
				}
			}
		}
	}
	
	public static void addAlerm(Context context, Alerm alerm){
		String action = alerm.notifyId;
		
		ALARM_ACTIONS.put(action, action);
		
		if(!ALARM_ACTIONS.contains(action)){
			return;
		}
		cancelAlerm(context, action);
		setAlerm(context, alerm);
		SharedPreferences sp = context.getSharedPreferences(Const.ALARM_SP, Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString(action, alerm.toJson());
		edit.commit();
	}

	public static void setAlerm(Context context, Alerm alerm){
		int dateMode = alerm.getMode();
		String startTime = alerm.getStartTime();
		String dateValue = alerm.getDateValue();
		long next = computeNextAlarm(dateMode, dateValue, startTime);
		Log.i("next", "------next="+next);
		Log.d("ldx", "nextTime: " + formatDate(next) + " , action: " + alerm.notifyId);
		Intent intent = new Intent(context, EAlarmReceiver.class);
		intent.setAction(alerm.notifyId);
		intent.putExtra(Const.KEY_DATA, alerm);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		if(next==0){
			am.set(AlarmManager.RTC_WAKEUP, alerm.start_time, pi);
		}else{
			am.setRepeating(AlarmManager.RTC_WAKEUP,alerm.start_time, next, pi);
		}
		System.out.println(alerm.start_time+"----------------"+next);
		System.out.println(alerm.start_time-next+"======");
	}
	
	public static void cancelAlerm(Context context, String action){
		Intent intent = new Intent(context, EAlarmReceiver.class);
		intent.setAction(action);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		am.cancel(pi);
	}
	
	public static void cancelAlermAll(Context context){
		for(Map.Entry<String, String> entry : ALARM_ACTIONS.entrySet()) {
			String action = entry.getValue();
			if (null != action) {
				cancelAlerm(context, action);
			}
		}
	}
	
	static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String formatDate(long time){
		String result = "";
		try{
			result = DateFormat.format(time);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 模式： <br>
	 * 1、DATE_MODE_FIX：指定日期，如20120301 , 参数dateValue格式：2012-03-01 <br>
	 * 2、DATE_MODE_WEEK：按星期提醒，如星期一、星期三 , 参数dateValue格式：1,3 <br>
	 * 3、DATE_MODE_MONTH：按月提醒，如3月2、3号，4月2、3号, 参数dateValue格式：3,4|2,3<br>
	 * 
	 * startTime:为当天开始时间，如上午9点, 参数格式为09:00
	 */
	public static long computeNextAlarm(int dateMode, String dateValue, String startTime) {
		final SimpleDateFormat fmt = new SimpleDateFormat();
		final Calendar c = Calendar.getInstance();
		final long now = System.currentTimeMillis();
		// 设置开始时间
		try {
			if (Alerm.DATE_MODE_FIX == dateMode) {
				fmt.applyPattern("yyyy-MM-dd");
				Date d = fmt.parse(dateValue);
				c.setTimeInMillis(d.getTime());
			}
			fmt.applyPattern("HH:mm");
			Date d = fmt.parse(startTime);
			c.set(Calendar.HOUR_OF_DAY, d.getHours());
			c.set(Calendar.MINUTE, d.getMinutes());
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long nextTime = 0;
		if (Alerm.DATE_MODE_FIX == dateMode) { // 按指定日期
			nextTime = c.getTimeInMillis();
			// 指定日期已过
			if (now >= nextTime){
				nextTime = 0;
			}
		} else if (Alerm.DATE_MODE_DAY == dateMode) { // 按日
			final int[] checkedWeeks = parseDateWeeks(dateValue);
			if (null != checkedWeeks) {
				for (int week : checkedWeeks) {
					c.set(Calendar.DAY_OF_WEEK, (week + 1));
					long triggerAtTime = c.getTimeInMillis();
					if (triggerAtTime <= now) { 
						triggerAtTime += AlarmManager.INTERVAL_DAY * 7;
					}
					// 保存最近闹钟时间
					if (0 == nextTime) {
						nextTime = triggerAtTime;
					} else {
						nextTime = Math.min(triggerAtTime, nextTime);
					}
				}
			}
		}  else if (Alerm.DATE_MODE_WEEK == dateMode) { // 按周
			final int[] checkedWeeks = parseDateWeeks(dateValue);
			if (null != checkedWeeks) {
				for (int week : checkedWeeks) {
					c.set(Calendar.DAY_OF_WEEK, (week + 1));
					long triggerAtTime = c.getTimeInMillis();
					if (triggerAtTime <= now) { // 下周
						triggerAtTime += AlarmManager.INTERVAL_DAY * 7;
					}
					// 保存最近闹钟时间
					if (0 == nextTime) {
						nextTime = triggerAtTime;
					} else {
						nextTime = Math.min(triggerAtTime, nextTime);
					}
				}
			}
		}else if (Alerm.DATE_MODE_MONTH == dateMode) { // 按月
			final int[][] items = parseDateMonthsAndDays(dateValue);
			final int[] checkedMonths = items[0];
			final int[] checkedDays = items[1];
			if (null != checkedDays && null != checkedMonths) {
				boolean isAdd = false;
				for (int month : checkedMonths) {
					c.set(Calendar.MONTH, (month - 1));
					for (int day : checkedDays) {
						c.set(Calendar.DAY_OF_MONTH, day);
						long triggerAtTime = c.getTimeInMillis();
						if (triggerAtTime <= now) { // 下一年
							c.add(Calendar.YEAR, 1);
							triggerAtTime = c.getTimeInMillis();
							isAdd = true;
						} else {
							isAdd = false;
						}
						if (isAdd) {
							c.add(Calendar.YEAR, -1);
						}
						// 保存最近闹钟时间
						if (0 == nextTime) {
							nextTime = triggerAtTime;
						} else {
							nextTime = Math.min(triggerAtTime, nextTime);
						}
					}
				}
			}
		}
		return nextTime;
	}

	public static int[] parseDateWeeks(String value) {
		int[] weeks = null;
	    try {
	        final String[] items = value.split(",");
	        weeks = new int[items.length];
	        int i = 0;
	        for (String s : items) {
	            weeks[i++] = Integer.valueOf(s);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return weeks;
	}

	public static int[][] parseDateMonthsAndDays(String value) {
		int[][] values = new int[2][];
	    try {
	        final String[] items = value.split("\\|");
	        final String[] monthStrs = items[0].split(",");
	        final String[] dayStrs = items[1].split(",");
	        values[0] = new int[monthStrs.length];
	        values[1] = new int[dayStrs.length];

	        int i = 0;
	        for (String s : monthStrs) {
	            values[0][i++] = Integer.valueOf(s);
	        }
	        i = 0;
	        for (String s : dayStrs) {
	            values[1][i++] = Integer.valueOf(s);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return values;
	}
	
	public static int guessMode(String interval){
		
		return INTERVAL.get(interval);
	}

	static{
		INTERVAL = new Hashtable<String, Integer>();
		INTERVAL.put("daily", Alerm.DATE_MODE_DAY);
		INTERVAL.put("weekly", Alerm.DATE_MODE_WEEK);
		INTERVAL.put("monthly", Alerm.DATE_MODE_MONTH);
		INTERVAL.put("yearly", Alerm.DATE_MODE_YEAR);
		INTERVAL.put("once", Alerm.DATE_MODE_ONCE);
		
		ALARM_ACTIONS = new Hashtable<String, String>();
	}
}