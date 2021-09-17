/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author mbechler
 *
 */
public class DigitsMatcher implements PasswordMatcher {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d{3,}"); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {
        Matcher matcher = DIGIT_PATTERN.matcher(password);

        List<MatchEntry> res = new ArrayList<>();
        int pos = 0;
        while ( matcher.find(pos) ) {
            res.add(new MatchEntry(password.substring(matcher.start(), matcher.end()), matcher.start(), MatchType.DIGITS));
            pos = matcher.end();
        }
        return res;
    }

}
