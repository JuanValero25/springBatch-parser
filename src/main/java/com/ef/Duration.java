package com.ef;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

enum Duration {

	HOURLY(200), DAILY(500);

	private int limit;

	private Duration(int limit) {
		this.limit = limit;
	}

	public boolean validateLimite(Integer thresholdLimit) {
		boolean validLimit = thresholdLimit > 0 && thresholdLimit <= limit;
		if (!validLimit)
			throw new RuntimeException("for " + this.name() + " duration cant be lower than 0 and bigger than " + limit);
		return validLimit;
	}
	
	public String  dateFrom(String stringDate, String dateFormate) {
		SimpleDateFormat sdf=new SimpleDateFormat(dateFormate);
		Date date;
		try {
			date = sdf.parse(stringDate);
			Calendar caledar = Calendar.getInstance();
			caledar.setTime(date);
			caledar.add(this.equals(DAILY)?Calendar.DATE:Calendar.HOUR, 1); 
			return sdf.format(caledar.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

}