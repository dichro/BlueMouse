package com.oldsch00l.BlueMouse;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;
import android.os.Bundle;

public class YaesuFormatter {
	public static final SimpleDateFormat DATE = new SimpleDateFormat(
			"HHmmss.SSS", Locale.UK);
	public static final DecimalFormat LATITUDE = new DecimalFormat(
			"0000.####", NMEAHelper.decSymFormat);
	public static final DecimalFormat LONGITUDE = new DecimalFormat(
			"00000.####", NMEAHelper.decSymFormat);
	public static final DecimalFormat SATELLITES = new DecimalFormat("00");
	public static final DecimalFormat ACCURACY = new DecimalFormat("00.0");
	public static final DecimalFormat ALTITUDE = new DecimalFormat("0000.0");
	public static final DecimalFormat GEOID = new DecimalFormat("000.0");

	public static String getNMEAGGA(final Location loc) {
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		double altitude = loc.getAltitude();
		Bundle bundle = loc.getExtras();
		int satellites = 0;
		if (bundle != null) {
			satellites = bundle.getInt("satellites", 7);
		}
		StringBuilder sentence = new StringBuilder().append("$GPGGA,")
				.append(DATE.format(new Date(loc.getTime())))
				.append(',')
				.append(convertPosition(LATITUDE, lat)).append(',')
				.append(lat >= 0 ? 'N' : 'S').append(',')
				.append(convertPosition(LONGITUDE, lng)).append(',')
				.append(lng >= 0 ? 'E' : 'W').append(',');
		sentence.append('1').append(','); // claim we always have a GPS fix
		sentence.append(SATELLITES.format(satellites)).append(',')
				.append(ACCURACY.format(loc.getAccuracy())).append(',')
				.append(ALTITUDE.format(altitude)).append(",M,")
				.append(GEOID.format(altitude)).append(",M,000.0,0000*");
		sentence.append(Integer.toHexString(NMEAHelper
				.getNMEAChecksum(sentence)));
		return sentence.append("\r\n").toString();
	}

	public static String convertPosition(DecimalFormat format, double position) {
		position = Math.abs(position);
		double degrees = Math.floor(position);
		return format.format(degrees * 100 + (position - degrees) * 60);
	}
}
