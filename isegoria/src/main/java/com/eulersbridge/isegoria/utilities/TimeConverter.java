package com.eulersbridge.isegoria.utilities;

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
		 Calendar c= Calendar.getInstance();
	     TimeZone tz = c.getTimeZone();
	     int offsetFromUtc = tz.getOffset(0)/1000;

		return timestamp + offsetFromUtc;
	}
}
