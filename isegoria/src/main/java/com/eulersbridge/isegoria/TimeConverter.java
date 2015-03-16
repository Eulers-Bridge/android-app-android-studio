package com.eulersbridge.isegoria;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class TimeConverter {
	public static String convertTimestampToString(long timestamp) {
		Date date = new Date(timestamp);

	    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");
	    
        return format.format(date);
	}
	
	public static long convertTimestampTimezone(long timestamp) {
		TimeZone timezone = TimeZone.getDefault();
		 Calendar c= Calendar.getInstance();
	     TimeZone tz = c.getTimeZone();
	     int offsetFromUtc = tz.getOffset(0)/1000;
	        
	    long newTime = timestamp + offsetFromUtc;
	      
	    return newTime;
	}
}
