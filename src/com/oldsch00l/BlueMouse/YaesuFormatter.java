package com.oldsch00l.BlueMouse;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;
import android.os.Bundle;

public class YaesuFormatter {
	private static final SimpleDateFormat TIME = new SimpleDateFormat(
			"HHmmss.SSS", Locale.UK);
	private static final SimpleDateFormat DATE = new SimpleDateFormat("ddmmyy",
			Locale.UK);
	private static final DecimalFormat LATITUDE = new DecimalFormat(
			"0000.####", NMEAHelper.decSymFormat);
	private static final DecimalFormat LONGITUDE = new DecimalFormat(
			"00000.####", NMEAHelper.decSymFormat);
	private static final DecimalFormat SATELLITES = new DecimalFormat("00");
	private static final DecimalFormat ACCURACY = new DecimalFormat("00.0",
			NMEAHelper.decSymFormat);
	private static final DecimalFormat F5_1 = new DecimalFormat("00000.0",
			NMEAHelper.decSymFormat);
	private static final DecimalFormat F4_2 = new DecimalFormat("0000.00",
			NMEAHelper.decSymFormat);
	private static final DecimalFormat F4_1 = new DecimalFormat("0000.0",
			NMEAHelper.decSymFormat);
	private static final DecimalFormat F3_2 = new DecimalFormat("000.00",
			NMEAHelper.decSymFormat);
	private static final DecimalFormat F3_1 = new DecimalFormat("000.00",
			NMEAHelper.decSymFormat);

	public static String getNMEAGGA(final Location loc) {
		double altitude = loc.getAltitude();
		Bundle bundle = loc.getExtras();
		int satellites = 0;
		if (bundle != null) {
			satellites = bundle.getInt("satellites", 7);
		}
		StringBuilder sentence = new StringBuilder().append("$GPGGA,")
				.append(TIME.format(new Date(loc.getTime()))).append(',');
		appendPosition(loc, sentence);
		sentence.append('1').append(','); // claim we always have a GPS fix
		sentence.append(SATELLITES.format(satellites)).append(',')
				.append(ACCURACY.format(loc.getAccuracy())).append(',')
				.append(F5_1.format(altitude)).append(",M,")
				.append(F4_1.format(altitude)).append(",M,000.0,0000*");
		sentence.append(Integer.toHexString(NMEAHelper
				.getNMEAChecksum(sentence)));
		return sentence.append("\r\n").toString();
	}

	public static String getNMEARMC(final Location loc) {
		Date date = new Date(loc.getTime());
		StringBuilder sentence = new StringBuilder().append("$GPRMC,")
				.append(TIME.format(date)).append(",A,");
		appendPosition(loc, sentence);
		sentence.append(F4_2.format(loc.getSpeed())).append(',')
				.append(F3_2.format(loc.getBearing())).append(',')
				.append(DATE.format(date)).append(",,");
		sentence.append(Integer.toHexString(NMEAHelper
				.getNMEAChecksum(sentence)));
		return sentence.append("\r\n").toString();
	}

	private static void appendPosition(final Location loc,
			StringBuilder sentence) {
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		sentence.append(convertPosition(LATITUDE, lat)).append(',')
				.append(lat >= 0 ? 'N' : 'S').append(',')
				.append(convertPosition(LONGITUDE, lng)).append(',')
				.append(lng >= 0 ? 'E' : 'W').append(',');
	}

	public static String convertPosition(DecimalFormat format, double position) {
		position = Math.abs(position);
		double degrees = Math.floor(position);
		return format.format(degrees * 100 + (position - degrees) * 60);
	}
}
