/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public final class FilterEscaping {

    private static final Logger log = Logger.getLogger(FilterEscaping.class);

    private static final String SLASH = "/"; //$NON-NLS-1$
    private static final String NULL_BYTE = "\0"; //$NON-NLS-1$
    private static final String BACKSLASH = "\\"; //$NON-NLS-1$
    private static final String CLOSE_PAREN = ")"; //$NON-NLS-1$
    private static final String OPEN_PAREN = "("; //$NON-NLS-1$
    private static final String STAR = "*"; //$NON-NLS-1$

    private static final String[] REPLACE_SPEC = new String[] {
        STAR, OPEN_PAREN, CLOSE_PAREN, BACKSLASH, NULL_BYTE, SLASH
    };

    private static final String[] REPLACEMENT_SPEC = new String[] {
        "\\2a", //$NON-NLS-1$
        "\\28", //$NON-NLS-1$
        "\\29",//$NON-NLS-1$
        "\\5c", //$NON-NLS-1$
        "\\00",//$NON-NLS-1$
        "\\2f" //$NON-NLS-1$
    };


    private FilterEscaping () {}


    /**
     * Escapes a filter attribute or value
     * 
     * @param data
     *            value to escape
     * @return escaped string
     */
    public static String escape ( String data ) {
        return StringUtils.replaceEach(data, REPLACE_SPEC, REPLACEMENT_SPEC);
    }


    /**
     * Unescapes a filter attribute or value
     * 
     * @param data
     *            escaped string
     * @return unescaped string
     */
    public static String unescape ( String data ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unescaping: " + data); //$NON-NLS-1$
        }
        Pattern escaped = Pattern.compile("\\\\[0-9a-f][0-9a-f]", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
        Matcher m = escaped.matcher(data);
        StringBuilder out = new StringBuilder();

        int lastMatchEnd = 0;

        while ( m.find() ) {
            final MatchResult matchResult = m.toMatchResult();
            if ( log.isDebugEnabled() ) {
                log.debug("Found escape sequence: " + matchResult.group()); //$NON-NLS-1$
            }
            out.append(data.substring(lastMatchEnd, matchResult.start()));
            String replacement = replaceEscape(matchResult.group());
            if ( log.isDebugEnabled() ) {
                log.debug("Replace with: " + replacement); //$NON-NLS-1$
            }
            out.append(replacement);
            lastMatchEnd = matchResult.end();

        }
        out.append(data.substring(lastMatchEnd));

        if ( log.isDebugEnabled() ) {
            log.debug("Unescaped: " + out.toString()); //$NON-NLS-1$
        }
        return out.toString();
    }


    private static String replaceEscape ( String escapeSequence ) {
        if ( escapeSequence.length() != 3 || escapeSequence.charAt(0) != '\\' ) {
            throw new IllegalArgumentException("Not a valid filter escape sequence: " + escapeSequence); //$NON-NLS-1$
        }

        byte code = Byte.valueOf(escapeSequence.substring(1), 16);
        return Character.toString((char) code);
    }
}
