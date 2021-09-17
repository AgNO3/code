/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.types.timezone;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.collections4.map.LRUMap;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;


/**
 * @author mbechler
 * 
 */
@Named ( "timeZoneInfo" )
@ApplicationScoped
public class TimeZoneInfo {

    private static final SortedSet<DateTimeZone> TIMEZONES = new TreeSet<>(new Comparator<DateTimeZone>() {

        @Override
        public int compare ( DateTimeZone o1, DateTimeZone o2 ) {
            final long curTime = System.currentTimeMillis();
            int res = Long.compare(o1.getStandardOffset(curTime), o2.getStandardOffset(curTime));

            if ( res != 0 ) {
                return res;
            }

            return o1.getID().compareTo(o2.getID());
        }

    });

    private static final int CACHE_SIZE = 10;
    private static final Map<Locale, List<DateTimeZoneInfo>> ITEM_CACHE = Collections
            .synchronizedMap(new LRUMap<Locale, List<DateTimeZoneInfo>>(CACHE_SIZE));
    private static final DateTimeFormatter OFFSET_FORMATTER = new DateTimeFormatterBuilder().appendTimeZoneOffset(null, true, 2, 4).toFormatter();


    static {
        for ( String tzId : DateTimeZone.getAvailableIDs() ) {
            DateTimeZone tz = DateTimeZone.forID(tzId);
            if ( tzId.equals(tz.getID()) ) {
                TIMEZONES.add(tz);
            }
        }
    }


    /**
     * 
     * @return the list of timezones known
     */
    public Set<DateTimeZone> getTimeZones () {
        return Collections.unmodifiableSet(TIMEZONES);
    }


    /**
     * 
     * @return a list of timezones for selection, localized in the current view locale
     */
    public List<DateTimeZoneInfo> getSelectItems () {
        return Collections.unmodifiableList(this.getSelectItems(FacesContext.getCurrentInstance().getViewRoot().getLocale()));
    }


    /**
     * 
     * @param l
     *            locale
     * @return a list of timezones for selection, in the given locale
     */
    public List<DateTimeZoneInfo> getSelectItems ( Locale l ) {

        List<DateTimeZoneInfo> cached = ITEM_CACHE.get(l);

        if ( cached == null ) {
            cached = makeSelectItems(l);
            ITEM_CACHE.put(l, cached);
        }

        return cached;

    }


    /**
     * @param l
     * @return
     */
    private List<DateTimeZoneInfo> makeSelectItems ( Locale l ) {
        List<DateTimeZoneInfo> res = new LinkedList<>();
        long curTime = System.currentTimeMillis();

        for ( DateTimeZone zone : this.getTimeZones() ) {
            long standardTime = getTimeForStandard(zone, curTime);
            DateTimeZoneInfo zInfo = new DateTimeZoneInfo(
                zone,
                zone.getID(),
                zone.getShortName(standardTime, l),
                zone.getName(standardTime, l),
                getTZOffset(zone, standardTime));
            res.add(zInfo);
        }

        return res;
    }


    /**
     * 
     * @param tz
     * @return the id for a timezone
     */
    public String getId ( DateTimeZone tz ) {

        if ( tz == null ) {
            return null;
        }
        return tz.getID();
    }


    /**
     * 
     * @param tz
     * @return the timezone id for displaying
     */
    public String getDisplayId ( DateTimeZone tz ) {
        if ( tz == null ) {
            return null;
        }
        return tz.getID().replace('_', ' ');
    }


    /**
     * 
     * @param tz
     * @return the short name for a timezone, localized in current view locale
     */
    public String getShortName ( DateTimeZone tz ) {
        if ( tz == null ) {
            return null;
        }
        return tz.getShortName(getTimeForStandard(tz, System.currentTimeMillis()), FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @param tz
     * @return the offset string for a timezone
     */
    public String getOffset ( DateTimeZone tz ) {
        if ( tz == null ) {
            return null;
        }
        return getTZOffset(tz, getTimeForStandard(tz, System.currentTimeMillis()));
    }


    private static String getTZOffset ( DateTimeZone zone, long curTime ) {
        return OFFSET_FORMATTER.withZone(zone).print(curTime);
    }


    private static long getTimeForStandard ( DateTimeZone zone, long curTime ) {
        long millis = curTime;
        while ( zone.getOffset(millis) != zone.getStandardOffset(millis) ) {
            long next = zone.nextTransition(millis);
            if ( next == millis ) {
                break;
            }
            millis = next;
        }
        return millis;
    }

}
