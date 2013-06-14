package name.gano.astro.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

public class TimeOrekit {

	private AbsoluteDate currentOrekitTime = null;

	// output format with timezone information
	private DateFormat dateFormat = null;


	private TimeScale timeScale = null;

	public TimeOrekit(GregorianCalendar currentDateTime) {

		// create the current absoluteDate

		try {
			this.timeScale = TimeScalesFactory.getUTC();
		} catch (OrekitException e) {
			System.out.println("ERROR CREATING THE CURRENT DATETIME:"
					+ e.toString());
		}

		DateComponents date = new DateComponents(
				currentDateTime.get(GregorianCalendar.YEAR),
				currentDateTime.get(GregorianCalendar.MONTH) + 1,
				currentDateTime.get(GregorianCalendar.DAY_OF_MONTH));

		TimeComponents time = new TimeComponents(
				currentDateTime.get(GregorianCalendar.HOUR_OF_DAY),
				currentDateTime.get(GregorianCalendar.MINUTE),
				currentDateTime.get(GregorianCalendar.SECOND)
						+ (currentDateTime.get(GregorianCalendar.MILLISECOND) / 1000.0));

		currentOrekitTime = new AbsoluteDate(date, time, this.timeScale);

	}

	public TimeOrekit(long currentTimeInMillis) throws OrekitException {

		this.timeScale = TimeScalesFactory.getUTC();

		this.currentOrekitTime = new AbsoluteDate(new AbsoluteDate(
				DateComponents.JAVA_EPOCH, TimeComponents.H00,
				TimeScalesFactory.getUTC()), currentTimeInMillis / 1000,
				TimeScalesFactory.getUTC());

	}

	public void update2CurrentTime() {

		GregorianCalendar currentDateTime = new GregorianCalendar(
				TimeZone.getTimeZone("UTC"));

		try {
			this.timeScale = TimeScalesFactory.getUTC();
		} catch (OrekitException e) {
			System.out.println("ERROR CREATING THE CURRENT DATETIME:"
					+ e.toString());
		}

		DateComponents date = new DateComponents(
				currentDateTime.get(GregorianCalendar.YEAR),
				currentDateTime.get(GregorianCalendar.MONTH) + 1,
				currentDateTime.get(GregorianCalendar.DAY_OF_MONTH));

		TimeComponents time = new TimeComponents(
				currentDateTime.get(GregorianCalendar.HOUR_OF_DAY),
				currentDateTime.get(GregorianCalendar.MINUTE),
				currentDateTime.get(GregorianCalendar.SECOND)
						+ (currentDateTime.get(GregorianCalendar.MILLISECOND) / 1000.0));

		this.currentOrekitTime = new AbsoluteDate(date, time, this.timeScale);

	}

	public String getDateTimeStr() {
		return dateFormat.format(this.currentOrekitTime.toDate(timeScale));
	}

	public void addSeconds(double value) {

		this.currentOrekitTime = this.currentOrekitTime.shiftedBy(value);

	}

	/**
	 * Sets dateformat timezone for the output string to use via the function
	 * getDateTimeStr()
	 * 
	 * @param dateStringFormat
	 *            time zone to format output strings with
	 */
	public void setDateStringFormat(TimeZone timeZone) {
		dateFormat.setTimeZone(timeZone);
	}

	/**
	 * Set SimpleDateFormat for displaying date/time string
	 * 
	 * @param dateFormat
	 *            SimpleDateFormat
	 */
	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * Set SimpleDateFormat string ISSUE - only valid after Jan 1, 1970
	 * 
	 * @param formatStr
	 *            String format for simple date format to use for creating
	 *            strings of the date
	 */
	public void setDateFormat(java.lang.String formatStr) {
		if ((formatStr != null) && (formatStr.length() > 0)) {
			dateFormat = new SimpleDateFormat(formatStr);
		}
	}

	/**
	 * Gets the date format
	 * 
	 * @return date format
	 */
	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public double getJulianDay() {
		return this.currentOrekitTime.durationFrom(AbsoluteDate.JULIAN_EPOCH) / 86400;
	}

	public double getModifiedJulianDay() {
		return this.currentOrekitTime
				.durationFrom(AbsoluteDate.MODIFIED_JULIAN_EPOCH) / 86400;
	}

	public AbsoluteDate getCurrentOrekitTime() {
		return currentOrekitTime;
	}

	public void setCurrentOrekitTime(AbsoluteDate currentOrekitTime) {
		this.currentOrekitTime = currentOrekitTime;
	}

	public void setCurentOrekitTime(GregorianCalendar currentDateTime) {
		// create the current absoluteDate

		DateComponents date = new DateComponents(
				currentDateTime.get(GregorianCalendar.YEAR),
				currentDateTime.get(GregorianCalendar.MONTH) + 1,
				currentDateTime.get(GregorianCalendar.DAY_OF_MONTH));

		TimeComponents time = new TimeComponents(
				currentDateTime.get(GregorianCalendar.HOUR_OF_DAY),
				currentDateTime.get(GregorianCalendar.MINUTE),
				currentDateTime.get(GregorianCalendar.SECOND)
						+ (currentDateTime.get(GregorianCalendar.MILLISECOND) / 1000.0));

		try {
			this.currentOrekitTime = new AbsoluteDate(date, time,
					TimeScalesFactory.getUTC());
		} catch (OrekitException ex) {
			System.out.println("ERROR CREATING THE CURRENT DATETIME:"
					+ ex.toString());
		}

	}

	public void setCurentOrekitTime(AbsoluteDate currentDateTime) {
		// create the current absoluteDate
		this.currentOrekitTime = currentDateTime;
	}

	public TimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(TimeScale timeScale) {
		this.timeScale = timeScale;
	}

}
