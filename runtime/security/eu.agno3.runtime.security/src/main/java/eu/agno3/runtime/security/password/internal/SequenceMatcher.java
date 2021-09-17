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
public class SequenceMatcher implements PasswordMatcher {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$
    private static final String DIGITS = "01234567890"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {
        List<MatchEntry> matches = new LinkedList<>();
        for ( int pos = 0; pos < password.length() - 1; ) {
            int next = pos + 1;

            String sequence = null;
            int direction;

            if ( ( direction = inSequence(LOWER, password, pos, next) ) != 0 ) {
                sequence = LOWER;
            }
            else if ( ( direction = inSequence(UPPER, password, pos, next) ) != 0 ) {
                sequence = UPPER;
            }
            else if ( ( direction = inSequence(DIGITS, password, pos, next) ) != 0 ) {
                sequence = DIGITS;
            }

            if ( sequence != null ) {
                // maximize run
                while ( true ) {
                    if ( next + 1 < password.length() && direction == inSequence(sequence, password, next, next + 1) ) {
                        next++;
                    }
                    else {
                        if ( next - pos > 2 ) {
                            matches.add(new SequenceMatchEntry(password.substring(pos, next + 1), pos, direction == -1));
                        }
                        break;
                    }
                }
            }
            pos = next;
        }

        return matches;
    }


    /**
     * @return the iteration direction (0 when not in sequence)
     */
    private static int inSequence ( String sequence, String password, int pos, int next ) {

        char curChar = password.charAt(pos);
        char nextChar = password.charAt(next);

        int curIdx = sequence.indexOf(curChar);
        int nextIdx = sequence.indexOf(nextChar);

        if ( curIdx < 0 || nextIdx < 0 ) {
            return 0;
        }

        int direction = curIdx - nextIdx;

        if ( direction == -1 || direction == 1 ) {
            return direction;
        }

        return 0;
    }
}
