/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.runtime.jsf.prefs;


import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.ocpsoft.prettytime.PrettyTime;

import eu.agno3.runtime.jsf.types.timezone.TimeZoneInfo;


/**
 * @author mbechler
 *
 */
public abstract class AbstractLocaleSettingsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6449353148481100143L;

    private static final Logger log = Logger.getLogger(AbstractLocaleSettingsBean.class);

    @Inject
    private UserPreferences userPreferences;

    @Inject
    private TimeZoneInfo tzInfo;


    protected abstract DateTimeZone getFallbackDefaultTimeZone ();

    private Map<Locale, PrettyTime> prettyTimeCache = new HashMap<>();


    /**
     * 
     */
    public AbstractLocaleSettingsBean () {
        super();
    }


    /**
     * @return the userLocale
     */
    public Locale getUserLocale () {
        Locale locale = this.userPreferences.getLocale();
        if ( log.isTraceEnabled() ) {
            log.trace("Locale is " + locale); //$NON-NLS-1$
        }
        if ( locale == null ) {
            log.warn("Preferences returned null locale"); //$NON-NLS-1$
            return Locale.ROOT;
        }
        return locale;
    }


    /**
     * @param date
     * @param style
     * @return the formatted datetime
     */
    public String formatDateTime ( DateTime date, String style ) {
        if ( date == null ) {
            return null;
        }

        DateTimeZone tz = getDateTimeZone();
        DateTime converted = date.withZone(tz);
        String patternForStyle = DateTimeFormat.patternForStyle(style, getDateLocale());
        return DateTimeFormat.forPattern(patternForStyle).print(converted);
    }


    /**
     * 
     * @param date
     * @param style
     * @return the formatted date
     */
    public String formatDate ( Date date, String style ) {
        return formatDateTime(new DateTime(date), style);
    }


    /**
     * 
     * @param date
     * @return the datettime in relative format
     */
    public String formatDateTimeRelative ( DateTime date ) {
        if ( date == null ) {
            return null;
        }

        PrettyTime t = getPrettyTime(getUserLocale());
        return t.format(date.toDate());
    }


    /**
     * @param userLocale
     * @return
     */
    private PrettyTime getPrettyTime ( Locale userLocale ) {
        PrettyTime cached = this.prettyTimeCache.get(userLocale);

        if ( cached != null ) {
            return cached;
        }

        cached = new PrettyTime(userLocale);
        this.prettyTimeCache.put(userLocale, cached);

        return cached;
    }


    /**
     * @return a time pattern for the user's language
     */
    public String getDateTimePattern () {
        return DateTimeFormat.patternForStyle("SM", getDateLocale()); //$NON-NLS-1$
    }


    /**
     * @return the java date time pattern to use
     */
    public String getDateFormatPattern () {
        return ( (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, getDateLocale()) ).toPattern();

    }


    /**
     * @return the java date time pattern to use
     */
    public String getTimeFormatPattern () {
        return ( (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM, getDateLocale()) ).toPattern();

    }


    /**
     * 
     * @param l
     * @param style
     * @return the formatted timestamp
     */
    public String formatTimestamp ( Long l, String style ) {
        if ( l == null ) {
            return null;
        }

        return formatDateTime(new DateTime(l), style);
    }


    /**
     * @param l
     * @return the formatted timestamp in relative format
     */
    public String formatTimestampRelative ( Long l ) {
        if ( l == null ) {
            return null;
        }
        return formatDateTimeRelative(new DateTime(l));
    }


    /**
     * @return the user's preferred date locale
     */
    public Locale getDateLocale () {
        if ( this.userPreferences.getOverrideDateLocale() != null ) {
            return this.userPreferences.getOverrideDateLocale();
        }
        return this.getUserLocale();
    }


    /**
     * @return the user datetimezone
     */
    public DateTimeZone getDateTimeZone () {
        if ( this.userPreferences.getOverrideTimezone() != null ) {
            return this.userPreferences.getOverrideTimezone();
        }

        return getFallbackDefaultTimeZone();
    }


    /**
     * 
     * @return the system default timezone
     */
    public DateTimeZone getDefaultDateTimeZone () {
        return getFallbackDefaultTimeZone();
    }


    /**
     * @return the default timezone short name
     */
    public String getDefaultTimeZoneDisplayName () {
        return this.tzInfo.getShortName(getFallbackDefaultTimeZone());
    }


    /**
     * @return the default timezone short name
     */
    public String getDefaultTimeZoneOffset () {
        return this.tzInfo.getOffset(getFallbackDefaultTimeZone());
    }


    /**
     * 
     * @return the user timezone
     */
    public TimeZone getTimeZone () {
        return this.getDateTimeZone().toTimeZone();
    }

}