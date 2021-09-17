/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class RepeatMatcher implements PasswordMatcher {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {
        List<MatchEntry> matches = new LinkedList<>();

        for ( int pos = 0; pos < password.length(); ) {
            int next = pos + 1;
            char curChar = password.charAt(pos);
            while ( true ) {
                if ( next < password.length() && password.charAt(next) == curChar ) {
                    next++;
                }
                else {
                    if ( next - pos > 2 ) {
                        matches.add(new MatchEntry(password.substring(pos, next), pos, MatchType.REPEAT));
                    }
                    break;
                }
            }

            pos = next;
        }

        return matches;
    }
}
