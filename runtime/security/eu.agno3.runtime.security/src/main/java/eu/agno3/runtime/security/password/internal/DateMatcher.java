/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author mbechler
 *
 */
public class DateMatcher implements PasswordMatcher {

    private static final Pattern YEAR_PATTERN = Pattern.compile("19\\d\\d|200\\d|201\\d/"); //$NON-NLS-1$
    private static final Pattern DATE_WITHOUT_SEP_PATTERN = Pattern.compile("\\d{4,8}"); //$NON-NLS-1$
    private static final Pattern DATE_SEP_PREFIX_PATTERN = Pattern
            .compile("(\\d{1,2})(\\s|-|/|\\\\|_|\\.)(\\d{1,2})\\2(19\\d{2}|200\\d|201\\d|\\d{2})"); //$NON-NLS-1$
    private static final Pattern DATE_SEP_SUFFIX_PATTERN = Pattern
            .compile("(19\\d{2}|200\\d|201\\d|\\d{2})(\\s|-|/|\\\\|_|\\.)(\\d{1,2})\\2(\\d{1,2})"); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {
        Collection<MatchEntry> matches = new LinkedList<>();
        addYearMatches(matches, password);
        addDateWithSepMatches(matches, password);
        addDateWithoutSepMatches(matches, password);
        return matches;
    }


    /**
     * @param matches
     * @param password
     */
    private static void addDateWithoutSepMatches ( Collection<MatchEntry> matches, String password ) {
        Matcher matcher = DATE_WITHOUT_SEP_PATTERN.matcher(password);
        int pos = 0;
        while ( matcher.find(pos) ) {
            int newPos = -1;
            if ( matcher.group().length() <= 6 ) {
                // try 2 digit year suffix
                newPos = Math.max(
                    newPos,
                    tryMatchWithoutSep(
                        matches,
                        password,
                        matcher,
                        pos,
                        matcher.group().substring(0, matcher.group().length() - 2),
                        Integer.parseInt(matcher.group().substring(matcher.group().length() - 2)),
                        true));

                // try 2 digit year prefix
                newPos = Math.max(
                    newPos,
                    tryMatchWithoutSep(
                        matches,
                        password,
                        matcher,
                        pos,
                        matcher.group().substring(2),
                        Integer.parseInt(matcher.group().substring(0, 2)),
                        true));
            }
            else {
                // try 4 digit year suffix
                newPos = Math.max(
                    newPos,
                    tryMatchWithoutSep(
                        matches,
                        password,
                        matcher,
                        pos,
                        matcher.group().substring(0, matcher.group().length() - 4),
                        Integer.parseInt(matcher.group().substring(matcher.group().length() - 4)),
                        false));

                // try 4 digit year prefix
                newPos = Math.max(
                    newPos,
                    tryMatchWithoutSep(
                        matches,
                        password,
                        matcher,
                        pos,
                        matcher.group().substring(4),
                        Integer.parseInt(matcher.group().substring(0, 4)),
                        false));
            }

            if ( newPos < 0 ) {
                pos += 1;
            }
            else {
                pos = newPos;
            }
        }
    }


    /**
     * @param matches
     * @param password
     * @param matcher
     * @param pos
     * @param dayOrMonth1
     * @param dayOrMonth2
     * @param year
     * @return
     */
    private static int tryMatchWithoutSep ( Collection<MatchEntry> matches, String password, Matcher matcher, int pos, String dayAndMonth, int year,
            boolean shortYear ) {
        int dayOrMonth1;
        int dayOrMonth2;
        if ( dayAndMonth.length() == 2 ) {
            dayOrMonth1 = Integer.parseInt(dayAndMonth.substring(0, 1));
            dayOrMonth2 = Integer.parseInt(dayAndMonth.substring(1));
        }
        else if ( dayAndMonth.length() >= 3 ) {
            dayOrMonth1 = Integer.parseInt(dayAndMonth.substring(0, 2));
            dayOrMonth2 = Integer.parseInt(dayAndMonth.substring(2));
        }
        else {
            return -1;
        }

        if ( validDate(dayOrMonth1, dayOrMonth2, year, shortYear) ) {
            matches.add(new DateMatchEntry(password.substring(matcher.start(), matcher.end()), matcher.start(), year, false));
            return matcher.end();
        }

        return -1;
    }


    /**
     * @param matches
     * @param password
     */
    private static void addDateWithSepMatches ( Collection<MatchEntry> matches, String password ) {
        Matcher matcher = DATE_SEP_PREFIX_PATTERN.matcher(password);
        int pos = 0;
        while ( matcher.find(pos) ) {
            int dayOrMonth1 = Integer.parseInt(matcher.group(1));
            int dayOrMonth2 = Integer.parseInt(matcher.group(3));
            int year = Integer.parseInt(matcher.group(4));

            if ( validDate(dayOrMonth1, dayOrMonth2, year, year <= 100) ) {
                matches.add(new DateMatchEntry(password.substring(matcher.start(), matcher.end()), matcher.start(), year, true));
            }

            pos = matcher.end();
        }
        pos = 0;

        matcher = DATE_SEP_SUFFIX_PATTERN.matcher(password);

        while ( matcher.find(pos) ) {
            int dayOrMonth1 = Integer.parseInt(matcher.group(1));
            int dayOrMonth2 = Integer.parseInt(matcher.group(3));
            int year = Integer.parseInt(matcher.group(4));

            if ( validDate(dayOrMonth1, dayOrMonth2, year, year <= 100) ) {
                matches.add(new DateMatchEntry(password.substring(matcher.start(), matcher.end()), matcher.start(), year, true));
            }

            pos = matcher.end();
        }
    }


    /**
     * @param dayOrMonth1
     * @param dayOrMonth2
     * @param year
     * @return
     */
    private static boolean validDate ( int dayOrMonth1, int dayOrMonth2, int year, boolean shortYear ) {
        if ( dayOrMonth1 > 31 || dayOrMonth2 > 31 ) {
            return false;
        }
        if ( dayOrMonth1 > 12 && dayOrMonth2 > 12 ) {
            return false;
        }

        // zxcvbn checks for year range in 1900 - 2019
        if ( !shortYear && ( year < 1900 || year > 2019 ) ) {
            return false;
        }

        return true;
    }


    /**
     * @param matches
     * @param password
     */
    private static void addYearMatches ( Collection<MatchEntry> matches, String password ) {
        Matcher matcher = YEAR_PATTERN.matcher(password);
        int pos = 0;
        while ( matcher.find(pos) ) {
            matches.add(new MatchEntry(password.substring(matcher.start(), matcher.end()), matcher.start(), MatchType.YEAR));
            pos = matcher.end();
        }
    }

}
