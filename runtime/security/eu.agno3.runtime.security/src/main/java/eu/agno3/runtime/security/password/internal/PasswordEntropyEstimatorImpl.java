/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.security.password.PasswordEntropyEstimator;


/**
 * @author mbechler
 *
 */
@Component ( service = PasswordEntropyEstimator.class )
public class PasswordEntropyEstimatorImpl implements PasswordEntropyEstimator {

    private static final Logger log = Logger.getLogger(PasswordEntropyEstimatorImpl.class);

    private final List<PasswordMatcher> matchers;


    /**
     * 
     */
    public PasswordEntropyEstimatorImpl () {
        this.matchers = new ArrayList<>();
        this.matchers.add(new SpatialMatcher());
        this.matchers.add(new SequenceMatcher());
        this.matchers.add(new RepeatMatcher());
        this.matchers.add(new DigitsMatcher());
        this.matchers.add(new DateMatcher());
        this.matchers.add(new CharacterPatternMatcher());
        this.matchers.add( ( new DictionaryLoader() ).getMatcher());
    }


    /**
     * @param matchers
     * 
     */
    public PasswordEntropyEstimatorImpl ( List<PasswordMatcher> matchers ) {
        this.matchers = matchers;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordEntropyEstimator#estimateEntropy(java.lang.String)
     */
    @Override
    public int estimateEntropy ( String password ) {
        if ( StringUtils.isEmpty(password) ) {
            return 0;
        }
        List<MatchEntry> matches = findMatches(password);
        Collections.sort(matches, new MatchEntryComparator());
        return (int) getMinimumEntropyMatch(password, matches);
    }


    /**
     * @param password
     * @return rule matches
     */
    public List<MatchEntry> findMatches ( String password ) {
        List<MatchEntry> matches = new ArrayList<>();
        for ( PasswordMatcher matcher : this.matchers ) {
            matches.addAll(matcher.match(password));
        }
        return matches;
    }


    private static float getMinimumEntropyMatch ( String password, List<MatchEntry> matches ) {
        if ( StringUtils.isBlank(password) ) {
            return 0;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Matches are " + matches); //$NON-NLS-1$
        }

        int bruteforceCardinality = PasswordUtil.getBruteforceCardinality(password);
        float bruteforceEntropy = PasswordUtil.log2(bruteforceCardinality);

        float minEntropyToPos[] = new float[password.length()];
        MatchEntry backpointers[] = new MatchEntry[password.length()];

        initSequence(password, matches, bruteforceEntropy, minEntropyToPos, backpointers);

        if ( log.isTraceEnabled() ) {
            log.trace("Password length is " + password.length()); //$NON-NLS-1$
            log.trace("Min entropy is" + Arrays.toString(minEntropyToPos)); //$NON-NLS-1$
            log.trace("Backpointers are " + Arrays.toString(backpointers)); //$NON-NLS-1$
            List<MatchEntry> matchSequence = buildMatchSequence(password, backpointers);
            log.trace("Match sequence is " + matchSequence); //$NON-NLS-1$
            matchSequence = fillInBruteforceMatches(password, bruteforceEntropy, bruteforceCardinality, matchSequence);
            log.trace("Filled match sequence is " + matchSequence); //$NON-NLS-1$
        }

        float entropy = minEntropyToPos[ password.length() - 1 ];

        if ( log.isTraceEnabled() ) {
            log.trace("Entropy is " + entropy); //$NON-NLS-1$
        }
        return entropy;
    }


    /**
     * @param password
     * @param bruteforceEntropy
     * @param matchSequence
     * @return
     */
    private static List<MatchEntry> fillInBruteforceMatches ( String password, float bruteforceEntropy, int bruteforceCardinality,
            List<MatchEntry> matchSequence ) {
        int at = 0;
        List<MatchEntry> filledWithBruteforce = new LinkedList<>();
        for ( MatchEntry e : matchSequence ) {
            if ( e.getStartPos() > at ) {
                int length = e.getStartPos() - at + 1;
                filledWithBruteforce.add(
                    new BruteforceMatchEntry(password.substring(at, e.getStartPos() - 1), at, length * bruteforceEntropy, bruteforceCardinality));
                at = e.getStartPos();

            }
            filledWithBruteforce.add(e);
            at = e.getEndPos() + 1;
        }

        if ( at < password.length() ) {
            int length = password.length() - at + 1;
            filledWithBruteforce.add(new BruteforceMatchEntry(password.substring(at - 1), at, length * bruteforceEntropy, bruteforceCardinality));
        }

        return filledWithBruteforce;
    }


    /**
     * @param password
     * @param backpointers
     * @return
     */
    private static List<MatchEntry> buildMatchSequence ( String password, MatchEntry[] backpointers ) {
        List<MatchEntry> sequence = new LinkedList<>();
        for ( int k = password.length() - 1; k >= 0; ) {
            if ( backpointers[ k ] != null ) {
                sequence.add(backpointers[ k ]);
                k = backpointers[ k ].getStartPos() - 1;
            }
            else {
                k--;
            }
        }
        Collections.reverse(sequence);
        return sequence;
    }


    /**
     * @param password
     * @param matches
     * @param bruteforceEntropy
     * @param minEntropyToPos
     * @param backpointers
     */
    private static void initSequence ( String password, List<MatchEntry> matches, float bruteforceEntropy, float[] minEntropyToPos,
            MatchEntry[] backpointers ) {
        for ( int pos = 1; pos <= password.length(); pos++ ) {
            minEntropyToPos[ pos - 1 ] = ( pos > 1 ? minEntropyToPos[ pos - 2 ] : 0 ) + bruteforceEntropy;

            for ( MatchEntry e : matches ) {
                if ( e.getEndPos() != pos ) {
                    continue;
                }

                float candidateEntropy = ( e.getStartPos() > 0 ? minEntropyToPos[ e.getStartPos() - 1 ] : 0 ) + e.getEntropy();

                if ( candidateEntropy < minEntropyToPos[ pos - 1 ] ) {
                    minEntropyToPos[ pos - 1 ] = candidateEntropy;
                    backpointers[ pos - 1 ] = e;
                }
            }
        }
    }
}
