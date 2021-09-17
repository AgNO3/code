/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.util.date;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;


/**
 * @author mbechler
 *
 */
@Named ( "dateFormatter" )
@ApplicationScoped
public class DateFormatter {

    private static final String MS_UNIT_SHORT = "ms"; //$NON-NLS-1$
    private static final String MIN_UNIT_SHORT = "min"; //$NON-NLS-1$
    private static final String HOUR_UNIT_SHORT = "h"; //$NON-NLS-1$
    private static final String DAY_UNIT_SHORT = "d"; //$NON-NLS-1$
    private static final String SECOND_UNIT_SHORT = "s"; //$NON-NLS-1$

    /**
     * 
     */
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+"); //$NON-NLS-1$


    /**
     * @param date
     * @return the formatted datetime
     */
    public String formatDateTimeLocal ( DateTime date ) {
        if ( date == null ) {
            return null;
        }
        String patternForStyle = DateTimeFormat.patternForStyle("MS", FacesContext.getCurrentInstance().getViewRoot().getLocale()); //$NON-NLS-1$
        return DateTimeFormat.forPattern(patternForStyle).print(date);
    }


    /**
     * @param obj
     * @return formatted duration
     */
    public String formatDurationObject ( Object obj ) {

        if ( ! ( obj instanceof Duration ) ) {
            return null;
        }

        return formatDuration(obj);

    }


    /**
     * @param obj
     * @return formatted duration
     */
    public String formatDuration ( Object obj ) {
        Duration dur = (Duration) obj;
        List<String> parts = new ArrayList<>();
        dur = handleDurationDays(dur, parts);
        dur = handleDurationHours(dur, parts);
        dur = handleDurationMinutes(dur, parts);
        dur = handleDurationSeconds(dur, parts);
        handleDurationMillis(dur, parts);
        if ( parts.isEmpty() ) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(parts, ' ');
    }


    /**
     * @param dur
     * @param parts
     */
    protected void handleDurationMillis ( Duration dur, List<String> parts ) {
        if ( dur.getMillis() != 0 ) {
            parts.add(formatDurationPart(dur.getMillis(), MS_UNIT_SHORT));
        }
    }


    /**
     * @param dur
     * @param parts
     * @return
     */
    protected Duration handleDurationSeconds ( Duration dur, List<String> parts ) {
        if ( dur.getStandardSeconds() != 0 ) {
            parts.add(formatDurationPart(dur.getStandardSeconds(), SECOND_UNIT_SHORT));
            return dur.minus(Duration.standardSeconds(dur.getStandardSeconds()));
        }
        return dur;
    }


    /**
     * @param standardSeconds
     * @param secondUnitShort
     * @return
     */
    private static String formatDurationPart ( long val, String unit ) {
        return String.format("%d %s", val, unit); //$NON-NLS-1$
    }


    /**
     * @param dur
     * @param parts
     * @return
     */
    protected Duration handleDurationMinutes ( Duration dur, List<String> parts ) {
        if ( dur.getStandardMinutes() != 0 ) {
            parts.add(formatDurationPart(dur.getStandardMinutes(), MIN_UNIT_SHORT));
            return dur.minus(Duration.standardMinutes(dur.getStandardMinutes()));
        }
        return dur;
    }


    /**
     * @param dur
     * @param parts
     * @return
     */
    protected Duration handleDurationHours ( Duration dur, List<String> parts ) {
        if ( dur.getStandardHours() != 0 ) {
            parts.add(formatDurationPart(dur.getStandardHours(), HOUR_UNIT_SHORT));
            return dur.minus(Duration.standardHours(dur.getStandardHours()));
        }
        return dur;
    }


    /**
     * @param dur
     * @param parts
     * @return
     */
    protected Duration handleDurationDays ( Duration dur, List<String> parts ) {
        if ( dur.getStandardDays() != 0 ) {
            parts.add(formatDurationPart(dur.getStandardDays(), DAY_UNIT_SHORT));
            return dur.minus(Duration.standardDays(dur.getStandardDays()));
        }
        return dur;
    }


    /**
     * @param str
     * @return parsed duration value
     */
    public static Duration parseDuration ( String str ) {
        Duration d = new Duration(0);

        try ( Scanner s = new Scanner(str) ) {
            s.useDelimiter(SPACE_PATTERN);
            while ( s.hasNextLong() ) {
                long val = s.nextLong();
                String unit = SECOND_UNIT_SHORT;
                if ( s.hasNext() ) {
                    unit = s.next();
                }

                switch ( unit ) {
                case DAY_UNIT_SHORT:
                    d = d.plus(Duration.standardDays(val));
                    break;
                case HOUR_UNIT_SHORT:
                    d = d.plus(Duration.standardHours(val));
                    break;
                case MIN_UNIT_SHORT:
                    d = d.plus(Duration.standardMinutes(val));
                    break;
                case SECOND_UNIT_SHORT:
                    d = d.plus(Duration.standardSeconds(val));
                    break;
                case MS_UNIT_SHORT:
                    d = d.plus(Duration.millis(val));
                    break;
                default:
                    throw new FacesException("Invalid duration unit specified: " + unit); //$NON-NLS-1$
                }
            }

            if ( s.hasNext() ) {
                throw new FacesException("Duration parse error: " + str); //$NON-NLS-1$
            }
        }
        return d;
    }
}
