/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.05.2015 by mbechler
 */
package eu.agno3.runtime.util.matching;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class PatternUtil {

    private static final Logger log = Logger.getLogger(PatternUtil.class);
    private static final int IN_ALTERNATIVE = 1;
    private static final Set<Character> ESCAPE_CHARS = new HashSet<>(Arrays.asList('.', '(', ')', '+', '|', '^', '$', '@', '%'));


    /**
     * 
     */
    private PatternUtil () {}


    /**
     * 
     * @param pattern
     * @param caseInsensitive
     * @return a regex pattern for the glob pattern
     */
    public static Pattern getGlobPattern ( String pattern, boolean caseInsensitive ) {

        boolean escaped = false;
        int state = 0;
        StringBuilder sb = new StringBuilder();

        if ( log.isDebugEnabled() ) {
            log.debug("Input pattern " + pattern); //$NON-NLS-1$
        }

        for ( char c : pattern.toCharArray() ) {
            switch ( c ) {
            case '\\':
                escaped = true;
                break;

            case '{':
                if ( state == IN_ALTERNATIVE || escaped ) {
                    sb.append("\\{"); //$NON-NLS-1$
                    escaped = false;
                    break;
                }
                sb.append('(');
                state = IN_ALTERNATIVE;
                break;
            case '}':
                if ( state != IN_ALTERNATIVE || escaped ) {
                    sb.append("\\}"); //$NON-NLS-1$
                    escaped = false;
                    break;
                }
                sb.append(')');
                state = 0;
                break;

            case ',':
                if ( state != IN_ALTERNATIVE || escaped ) {
                    sb.append(","); //$NON-NLS-1$
                    escaped = false;
                    break;
                }
                sb.append('|');
                break;

            case '?':
                if ( escaped ) {
                    sb.append("\\?"); //$NON-NLS-1$
                    escaped = false;
                    break;
                }
                sb.append('.');
                break;
            case '*':
                if ( escaped ) {
                    sb.append("\\*"); //$NON-NLS-1$
                    escaped = false;
                    break;
                }
                sb.append(".*"); //$NON-NLS-1$
                break;

            default:
                if ( ESCAPE_CHARS.contains(c) ) {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            }

        }

        String regex = sb.toString();
        if ( log.isDebugEnabled() ) {
            log.debug("Output regex " + regex); //$NON-NLS-1$
        }
        return Pattern.compile(regex, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
    }


    /**
     * 
     * @param pattern
     * @return a SQL like pattern (\ as escape character)
     */
    public static String getLikePattern ( String pattern ) {
        return getLikePattern(pattern, '\\');
    }


    /**
     * 
     * @param pattern
     * @param escapeChar
     * @return a SQL like pattern
     */
    public static String getLikePattern ( String pattern, char escapeChar ) {

        boolean escaped = false;
        StringBuilder sb = new StringBuilder();
        for ( char c : pattern.toCharArray() ) {

            switch ( c ) {
            case '\\':
                if ( escaped ) {
                    sb.append(escapeChar);
                    sb.append('\\');
                    escaped = false;
                    break;
                }
                escaped = true;
                break;
            case '*':
                if ( escaped ) {
                    sb.append('*');
                    escaped = false;
                    break;
                }
                sb.append('%');
                break;
            case '?':
                if ( escaped ) {
                    sb.append('?');
                    escaped = false;
                    break;
                }
                sb.append('_');
                break;
            case '%':
            case '_':
            case '[':
            case ']':
                sb.append(escapeChar);
                sb.append(c);
                escaped = false;
                break;
            default:
                sb.append(c);
                escaped = false;
                break;
            }
        }

        return sb.toString();
    }


    /**
     * Adds wildcards to fron and back if the query does not start with either a wildcard or end with one
     * 
     * @param query
     * @return modified query
     */
    public static String makeSubstringQuery ( String query ) {
        String pattern = query;

        if ( pattern.charAt(0) != '*' && pattern.charAt(pattern.length() - 1) != '*' ) {
            if ( pattern.charAt(0) != '*' ) {
                pattern = '*' + pattern;
            }

            if ( pattern.charAt(pattern.length() - 1) != '*' ) {
                pattern = pattern + '*';
            }
        }
        return pattern;
    }
}
