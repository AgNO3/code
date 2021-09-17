/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;


/**
 * @author mbechler
 *
 */
public class DictionaryMatchEntry extends MatchEntry {

    private static final Pattern START_UPPER = Pattern.compile("^[A-Z][^A-Z]+$"); //$NON-NLS-1$
    private static final Pattern END_UPPER = Pattern.compile("^[^A-Z]+[A-Z]$"); //$NON-NLS-1$
    private static final Pattern ALL_UPPER = Pattern.compile("^[^a-z]+$"); //$NON-NLS-1$
    private static final Pattern ALL_LOWER = Pattern.compile("^[^A-Z]+$"); //$NON-NLS-1$

    private final int rank;
    private final boolean l33t;
    private final Map<Character, Character> subsitution;
    private String dictionaryName;


    /**
     * @param token
     * @param startPos
     * @param rank
     * @param dictionaryName
     * @param l33t
     * @param subsitutions
     */
    public DictionaryMatchEntry ( String token, int startPos, int rank, String dictionaryName, boolean l33t,
            Map<Character, Character> subsitutions ) {
        super(token, startPos);
        this.rank = rank;
        this.dictionaryName = dictionaryName;
        this.l33t = l33t;
        this.subsitution = subsitutions;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getType()
     */
    @Override
    public MatchType getType () {
        return MatchType.DICTIONARY;
    }


    /**
     * @return the dictionaryName
     */
    public String getDictionaryName () {
        return this.dictionaryName;
    }


    /**
     * @return the rank
     */
    public int getRank () {
        return this.rank;
    }


    /**
     * @return the l33t
     */
    public boolean isL33t () {
        return this.l33t;
    }


    /**
     * @return the substitution map
     */
    public Map<Character, Character> getSubstitution () {
        return this.subsitution;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#calcEntropy()
     */
    @Override
    protected float calcEntropy () {
        return PasswordUtil.log2(this.getRank()) + calcUppercaseEntropy() + calcL33tEntropy();
    }


    /**
     * @return entropy
     */
    public float calcL33tEntropy () {

        if ( !this.isL33t() || this.getSubstitution() == null ) {
            return 0;
        }

        int possibilities = 0;

        for ( Entry<Character, Character> e : this.getSubstitution().entrySet() ) {
            int countUnsub = PasswordUtil.countChars(this.getToken(), e.getKey());
            int countSub = PasswordUtil.countChars(this.getToken(), e.getValue());
            if ( countSub == 0 ) {
                continue;
            }
            possibilities += PasswordUtil.getPossibilities(countUnsub, countSub);
        }

        if ( possibilities <= 1 ) {
            return 1;
        }
        return PasswordUtil.log2(possibilities);
    }


    /**
     * @return entropy
     */
    public float calcUppercaseEntropy () {
        if ( ALL_LOWER.matcher(this.getToken()).matches() ) {
            return 0;
        }

        if ( ALL_UPPER.matcher(this.getToken()).matches() || START_UPPER.matcher(this.getToken()).matches()
                || END_UPPER.matcher(this.getToken()).matches() ) {
            return 1;
        }
        int countLower = PasswordUtil.countClass(this.getToken(), CharacterClass.LOWERCASE);
        int countUpper = PasswordUtil.countClass(this.getToken(), CharacterClass.UPPERCASE);
        return PasswordUtil.log2(PasswordUtil.getPossibilities(countUpper, countLower));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#toString()
     */
    @Override
    public String toString () {
        return String.format("%s dict %s rank %d", super.toString(), this.getDictionaryName(), this.getRank()); //$NON-NLS-1$
    }
}
