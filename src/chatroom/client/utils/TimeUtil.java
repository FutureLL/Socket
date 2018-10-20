package chatroom.client.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	//2016-10-27 19:20:12  yyyy-MM-dd HH:mm:ss
	public static String changeDate2Week(String dateStr,String formatStr){
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SimpleDateFormat sdf1 = new SimpleDateFormat("E");
		return sdf1.format(date);
	}
	
	//把字符串的日期转为毫秒数
	public static long changeDate2Mils(String dateStr,String formatStr){
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}
	
	//把毫秒数转为指定的格式的日期
	public static String changeMils2Date(long mils,String formatStr){
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		Date date = new Date(mils);
		return sdf.format(date);
	}
	
	public static String changeDate2CustomerMsg(String dateStr){
		long mils = changeDate2Mils(dateStr,"yyyy-MM-dd_HH:mm:ss");
		long nowMils = System.currentTimeMillis();
		long offsetMils = nowMils - mils;
		String msg = "";
		if(offsetMils<1000*60*2){
			msg = "刚刚";
		}else if(offsetMils<1000*60*60){
			msg = (offsetMils/(1000*60))+"分钟前";
		}else if(offsetMils<1000*60*60*24){
			msg = (offsetMils/(1000*60*60))+"小时前";
		}else if(offsetMils<1000*60*60*24*2){
			msg = "昨天";
		}else{
			String nowYear = changeMils2Date(nowMils, "yyyy");
			String year = changeMils2Date(mils, "yyyy");
			if(nowYear.equals(year)){
				msg = changeMils2Date(mils, "MM月dd日");
			}else{
				msg = changeMils2Date(mils, "yyyy年MM月dd日");
			}
		}
		return msg;
	}
	
}
