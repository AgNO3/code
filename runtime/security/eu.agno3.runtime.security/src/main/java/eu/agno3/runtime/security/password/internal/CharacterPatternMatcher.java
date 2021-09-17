/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class CharacterPatternMatcher implements PasswordMatcher {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {
        Collection<MatchEntry> matches = new ArrayList<>();
        Set<Character> found = new HashSet<>();
        for ( int pos = 0; pos < password.length() - 3; ) {
            found.clear();

            int next = pos;
            int len = 0;
            while ( next < password.length() && ( len < 4 || ( (float) len / found.size() ) >= 1.5 ) ) {
                found.add(password.charAt(next));
                next++;
                len = next - pos;
            }

            if ( len >= 4 && ( (float) len / found.size() ) >= 1.5 ) {
                matches.add(new CharacterPatternMatchEntry(password.substring(pos, next), pos, found));
                pos = next;
                continue;
            }

            pos++;
        }

        return matches;
    }
}
