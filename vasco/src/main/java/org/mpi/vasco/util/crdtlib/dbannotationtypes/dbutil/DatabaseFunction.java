package org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mpi.vasco.util.commonfunc.StringOperations;

// TODO: Auto-generated Javadoc
/**
 * The Class DatabaseFunction.
 */
public class DatabaseFunction {
	
	public static DateFormat getNewDateFormatInstance() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat;
	}

	/**
	 * Now.
	 *
	 * @return the timestamp
	 */
	/*public static Timestamp NOW() {
		java.util.Date utilDate = new java.util.Date();
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
		return sqlDate;
	}*/
	
	public static Date NOW() {
		java.util.Date utilDate = new java.util.Date();
		return utilDate;
	}

	/**
	 * Currenttimestamp.
	 *
	 * @return the timestamp
	 */
	public static String CURRENTTIMESTAMP(DateFormat dateFormat) {
		return dateFormat.format(NOW());
	}
	
	/**
	 * Convert timestamp to date.
	 *
	 * @param ts the ts
	 * @return the date
	 */
	public static Date convertTimestampToDate(Timestamp ts) {
		Date date = new Date(ts.getTime());
		return date;
	}
	
	/**
	 * Convert timestamp to date str.
	 *
	 * @param ts the ts
	 * @return the string
	 */
	public static String convertTimestampToDateStr(DateFormat dateFormat, Timestamp ts) {
		Date date = new Date(ts.getTime());
		return dateFormat.format(date);
	}
	
	/**
	 * Covert timestamp long to date.
	 *
	 * @param ts the ts
	 * @return the date
	 */
	public static Date covertTimestampLongToDate(long ts) {
		Date date = new Date(ts);
		return date;
	}
	
	/**
	 * Covert timestamp long to date str.
	 *
	 * @param ts the ts
	 * @return the string
	 */
	public static String covertTimestampLongToDateStr(DateFormat dateFormat, long ts) {
		Date date = new Date(ts);
		return dateFormat.format(date);
	}
	
	/**
	 * Convert date str to long.
	 *
	 * @param dateStr the date str
	 * @return the long
	 */
	public static long convertDateStrToLong(DateFormat dateFormat, String dateStr) {
		Date date = null;
		try {
				date = dateFormat.parse(StringOperations.removeQuotesFromHeadTail(dateStr));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date.getTime();
	}
}