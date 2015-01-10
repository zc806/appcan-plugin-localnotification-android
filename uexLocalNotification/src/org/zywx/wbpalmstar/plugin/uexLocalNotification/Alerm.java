package org.zywx.wbpalmstar.plugin.uexLocalNotification;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

public class Alerm implements Serializable{
	

	public static final int DATE_MODE_FIX 	= 0;
	public static final int DATE_MODE_DAY 	= 1;
	public static final int DATE_MODE_WEEK 	= 2;
	public static final int DATE_MODE_MONTH = 3;
	public static final int DATE_MODE_YEAR 	= 4;
	public static final int DATE_MODE_ONCE 	= 5;
	
	
	private static final long serialVersionUID = 7198863596810633314L;
	
	public String notifyId;
	public String title;
	public String content;
	public int minute;
	public int hour;
	public int month;
	public int day_of_month;
	public int day_of_week;
	public int day_of_year;
	public String interval;
	public long start_time;;
	public String ringName;
	public String mode;
	
	public Alerm(){
		;
	}
	
	public void setStartTime(long s){
		start_time = s;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(start_time);
		int m = c.get(Calendar.MINUTE);
		int h = c.get(Calendar.HOUR_OF_DAY);
		int mo = c.get(Calendar.MONTH);
		int day_of_m = c.get(Calendar.DAY_OF_MONTH);
		int day_of_w = c.get(Calendar.DAY_OF_WEEK);
		int day_of_y = c.get(Calendar.DAY_OF_YEAR);
		hour = h;
		minute = m;
		month = mo;
		day_of_month = day_of_m;
		day_of_week = day_of_w;
		day_of_year = day_of_y;
	}
	
	public String getStartTime(){
		String h = "";
		if(hour < 10){
			h = "0" + hour;
		}else{
			h = "" + hour;
		}
		String m = "";
		if(minute < 10){
			m = "0" + minute;
		}else{
			m = "" + minute;
		}
		return h + ":" + m;
	}
	
	public String getDateValue(){
		String result = null;
		int mode = getMode();
		switch (mode) {
		case DATE_MODE_FIX:
			result = formatDate(start_time);
			break;
		case DATE_MODE_DAY:
			result = "1,2,3,4,5,6,7";
			break;
		case DATE_MODE_WEEK:
			result = "" + (day_of_week - 1);
			break;
		case DATE_MODE_MONTH:
			result = "1,2,3,4,5,6,7,8,9,10,11,12|" + day_of_month;
			break;
		case DATE_MODE_YEAR:
			result = "" + day_of_year;
			break;
		case DATE_MODE_ONCE:
			result = "";
			break;	
		}
		return result;
	}
	
	static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static String formatDate(long time){
		String result = "";
		try{
			result = DateFormat.format(time);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int getMode(){
		
		return EAlarmReceiver.guessMode(interval);
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("notifyId", notifyId);
			json.put("title", title);
			json.put("content", content);
			json.put("minute", minute);
			json.put("hour", hour);
			json.put("month", month);
			json.put("day_of_month", day_of_month);
			json.put("day_of_week", day_of_week);
			json.put("day_of_year", day_of_year);
			json.put("interval", interval);
			json.put("ringName", ringName);
			json.put("start_time", start_time);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public static Alerm parserJson(String jsonStr){
		Alerm alerm = null;
		try{
			JSONObject json = new JSONObject(jsonStr);
			alerm = new Alerm();
			alerm.notifyId = json.optString("notifyId");
			alerm.title = json.optString("title");
			alerm.content = json.optString("content");
			alerm.minute = json.optInt("minute");
			alerm.hour = json.optInt("hour");
			alerm.month = json.optInt("month");
			alerm.day_of_month = json.optInt("day_of_month");
			alerm.day_of_week = json.optInt("day_of_week");
			alerm.day_of_year = json.optInt("day_of_year");
			alerm.interval = json.optString("interval");
			alerm.ringName = json.optString("ringName");
			alerm.start_time = json.optLong("start_time");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return alerm;
	}
}