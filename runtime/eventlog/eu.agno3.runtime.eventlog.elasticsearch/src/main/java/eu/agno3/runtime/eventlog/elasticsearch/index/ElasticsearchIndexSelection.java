/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch.index;


import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import eu.agno3.runtime.eventlog.IndexType;


/**
 * @author mbechler
 *
 */
public class ElasticsearchIndexSelection {

    private static final DateTimeFormatter INDEX_DAILY_DATE_FORMATTER = ( new DateTimeFormatterBuilder() ).appendYear(4, 4).appendMonthOfYear(2)
            .appendDayOfMonth(2).toFormatter();

    private static final DateTimeFormatter INDEX_WEEKLY_DATE_FORMATTER = ( new DateTimeFormatterBuilder() ).appendWeekyear(4, 4)
            .appendWeekOfWeekyear(2).toFormatter();

    private static final DateTimeFormatter INDEX_MONTHLY_DATE_FORMATTER = ( new DateTimeFormatterBuilder() ).appendYear(4, 4).appendMonthOfYear(2)
            .toFormatter();

    private static final DateTimeFormatter INDEX_YEARLY_DATE_FORMATTER = ( new DateTimeFormatterBuilder() ).appendYear(4, 4).toFormatter();


    /**
     * @param type
     * @param indexBase
     * @param eventTime
     * @return the index name for a store
     */
    public static String makeIndex ( IndexType type, String indexBase, DateTime eventTime ) {
        return indexBase.concat(eventTime.toString(getDateFormat(type)));
    }


    /**
     * @param type
     * @return the date formatter for the index type
     */
    public static DateTimeFormatter getDateFormat ( IndexType type ) {
        DateTimeFormatter format;
        switch ( type ) {
        case DAILY:
            format = INDEX_DAILY_DATE_FORMATTER;
            break;
        case MONTHLY:
            format = INDEX_MONTHLY_DATE_FORMATTER;
            break;
        case WEEKLY:
            format = INDEX_WEEKLY_DATE_FORMATTER;
            break;
        case YEARLY:
            format = INDEX_YEARLY_DATE_FORMATTER;
            break;
        default:
            throw new IllegalArgumentException();
        }
        return format;
    }


    /**
     * 
     * @param type
     * @param indexBase
     * @param limit
     *            maximum number of individual patterns to return
     * @param startTime
     * @param endTime
     * @return generated index patterns
     */
    public static String[] getIndexPatternsForRange ( IndexType type, String indexBase, int limit, DateTime startTime, DateTime endTime ) {
        DateTime startDay = startTime.withMillisOfDay(0);
        DateTime endDay = ( endTime != null ? endTime : DateTime.now() ).withMillisOfDay(0);

        Duration diff = new Duration(startDay, endDay);
        List<String> subPatterns = new LinkedList<>();

        switch ( type ) {
        case DAILY:
            makeIndexPatternsDaily(type, indexBase, limit, startDay, endDay, diff, subPatterns);
            break;
        case MONTHLY:
            makeIndexPatternsMonthly(type, indexBase, limit, startDay, endDay, diff, subPatterns);
            break;
        case WEEKLY:
            makeIndexPatternsWeekly(type, indexBase, limit, startDay, endDay, diff, subPatterns);
            break;
        case YEARLY:
            makeIndexPatternsYearly(type, indexBase, limit, startDay, endDay, diff, subPatterns);
            break;
        default:
            throw new IllegalArgumentException();
        }

        return subPatterns.toArray(new String[subPatterns.size()]);
    }


    /**
     * @param type
     * @param indexBase
     * @param limit
     * @param startDay
     * @param endDay
     * @param diff
     * @param subPatterns
     */
    private static void makeIndexPatternsYearly ( IndexType type, String indexBase, int limit, DateTime startDay, DateTime endDay, Duration diff,
            List<String> subPatterns ) {
        int startYear = startDay.getYear();
        int endYear = endDay.getYear();
        for ( int i = startYear; i <= endYear; i++ ) {
            subPatterns.add(String.format("%s%04d", indexBase, i)); //$NON-NLS-1$
        }
    }


    /**
     * @param type
     * @param indexBase
     * @param limit
     * @param startDay
     * @param endDay
     * @param diff
     * @param subPatterns
     */
    private static void makeIndexPatternsWeekly ( IndexType type, String indexBase, int limit, DateTime startDay, DateTime endDay, Duration diff,
            List<String> subPatterns ) {

        int startWeekyear = startDay.getWeekyear();
        int endWeekyear = endDay.getWeekyear();
        int startWeek = startDay.getWeekOfWeekyear();
        int endWeek = endDay.getWeekOfWeekyear();

        if ( diff.getStandardDays() / 7 > limit ) {
            for ( int i = startWeekyear; i <= endWeekyear; i++ ) {
                subPatterns.add(String.format("%s%04d*", indexBase, i)); //$NON-NLS-1$
            }
        }
        else if ( startWeekyear != endWeekyear ) {
            addWeeks(indexBase, subPatterns, startWeekyear, startWeek, startDay.weekOfWeekyear().getMaximumValue());
            for ( int i = startWeekyear + 1; i < endWeekyear; i++ ) {
                subPatterns.add(String.format("%s%04d*", indexBase, i)); //$NON-NLS-1$
            }
            addWeeks(indexBase, subPatterns, endWeekyear, endDay.weekOfWeekyear().getMinimumValue(), endWeek);
        }
        else {
            addWeeks(indexBase, subPatterns, startWeekyear, startWeek, endWeek);
        }

    }


    /**
     * @param indexBase
     * @param subPatterns
     * @param weekyear
     * @param startWeek
     * @param endWeek
     */
    private static void addWeeks ( String indexBase, List<String> subPatterns, int weekyear, int startWeek, int endWeek ) {
        for ( int i = startWeek; i <= endWeek; i++ ) {
            subPatterns.add(String.format("%s%04d%02d", indexBase, weekyear, i)); //$NON-NLS-1$
        }
    }


    /**
     * @param type
     * @param indexBase
     * @param limit
     * @param startDay
     * @param endDay
     * @param diff
     * @param subPatterns
     */
    private static void makeIndexPatternsMonthly ( IndexType type, String indexBase, int limit, DateTime startDay, DateTime endDay, Duration diff,
            List<String> subPatterns ) {

        int year = startDay.getYear();
        int endYear = endDay.getYear();
        int startMonth = startDay.getMonthOfYear();
        int endMonth = endDay.getMonthOfYear();
        if ( diff.getStandardDays() > limit * 31L ) {
            for ( int i = year; i <= endYear; i++ ) {
                subPatterns.add(String.format("%s%04d*", indexBase, i)); //$NON-NLS-1$
            }
        }
        else if ( year != endYear ) {
            addMonthsMonthly(indexBase, subPatterns, year, startMonth, startDay.monthOfYear().getMaximumValue());
            for ( int i = year + 1; i < endYear; i++ ) {
                subPatterns.add(String.format("%s%04d", indexBase, i)); //$NON-NLS-1$
            }
            addMonthsMonthly(indexBase, subPatterns, endYear, startDay.monthOfYear().getMinimumValue(), endMonth);
        }
        else {
            addMonthsMonthly(indexBase, subPatterns, year, startMonth, endMonth);
        }
    }


    /**
     * @param indexBase
     * @param subPatterns
     * @param year
     * @param startMonth
     * @param endMonth
     */
    private static void addMonthsMonthly ( String indexBase, List<String> subPatterns, int year, int startMonth, int endMonth ) {
        for ( int i = startMonth; i <= endMonth; i++ ) {
            subPatterns.add(String.format("%s%04d%02d", indexBase, year, i)); //$NON-NLS-1$
        }
    }


    /**
     * @param type
     * @param indexBase
     * @param limit
     * @param startDay
     * @param endDay
     * @param diff
     * @param subPatterns
     */
    private static void makeIndexPatternsDaily ( IndexType type, String indexBase, int limit, DateTime startDay, DateTime endDay, Duration diff,
            List<String> subPatterns ) {
        if ( diff.getStandardDays() > limit * 31 ) {
            // match on years
            createYearPatterns(indexBase, startDay, endDay, subPatterns);
        }
        else if ( diff.getStandardDays() > limit ) {
            // match on months
            createMonthPatterns(indexBase, startDay, endDay, subPatterns);
        }
        else {
            // match on days
            DateTime date = startDay;
            while ( date.isBefore(endDay) ) {
                subPatterns.add(makeIndex(type, indexBase, date));
                date = date.plusDays(1);
            }
            subPatterns.add(makeIndex(type, indexBase, endDay));
        }
    }


    /**
     * @param indexBase
     * @param startDay
     * @param subPatterns
     */
    private static void createMonthPatterns ( String indexBase, DateTime startDay, DateTime endDay, List<String> subPatterns ) {
        int year = startDay.getYear();
        int endYear = endDay.getYear();
        int startMonth = startDay.getMonthOfYear();
        int endMonth = endDay.getMonthOfYear();

        if ( year != endYear ) {
            addMonths(indexBase, subPatterns, year, startMonth, startDay.monthOfYear().getMaximumValue());
            for ( int i = year + 1; i < endYear; i++ ) {
                subPatterns.add(String.format("%s%04d*", indexBase, i)); //$NON-NLS-1$
            }
            addMonths(indexBase, subPatterns, endYear, startDay.monthOfYear().getMinimumValue(), endMonth);
        }
        else {
            addMonths(indexBase, subPatterns, year, startMonth, endMonth);
        }
    }


    /**
     * @param indexBase
     * @param subPatterns
     * @param startYear
     * @param startMonth
     * @param endMonth
     */
    private static void addMonths ( String indexBase, List<String> subPatterns, int startYear, int startMonth, int endMonth ) {
        for ( int i = startMonth; i <= endMonth; i++ ) {
            subPatterns.add(String.format("%s%04d%02d*", indexBase, startYear, i)); //$NON-NLS-1$
        }
    }


    /**
     * @param indexBase
     * @param startDay
     * @param endDay
     * @param subPatterns
     */
    private static void createYearPatterns ( String indexBase, DateTime startDay, DateTime endDay, List<String> subPatterns ) {
        int startYear = startDay.getYear();
        int endYear = endDay.getYear();
        for ( int i = startYear; i <= endYear; i++ ) {
            subPatterns.add(String.format("%s%04d*", indexBase, i)); //$NON-NLS-1$
        }
    }
}
