/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


/**
 * @author mbechler
 *
 */
public class SequenceMatchEntry extends MatchEntry {

    private final boolean ascending;


    /**
     * @param token
     * @param startPos
     * @param ascending
     */
    public SequenceMatchEntry ( String token, int startPos, boolean ascending ) {
        super(token, startPos);
        this.ascending = ascending;
    }


    /**
     * @return the ascending
     */
    public boolean isAscending () {
        return this.ascending;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getType()
     */
    @Override
    public MatchType getType () {
        return MatchType.SEQUENCE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#calcEntropy()
     */
    @Override
    protected float calcEntropy () {
        char firstChar = this.getToken().charAt(0);
        float baseEntropy = 0.0f;

        CharacterClass firstClass = PasswordUtil.getCharacterClass(firstChar);
        if ( firstChar == 'a' || firstChar == '1' ) {
            baseEntropy = 1.0f;
        }
        else if ( firstClass == CharacterClass.DIGIT ) {
            baseEntropy = PasswordUtil.log2(10);
        }
        else if ( firstClass == CharacterClass.LOWERCASE ) {
            baseEntropy = PasswordUtil.log2(26);
        }
        else if ( firstClass == CharacterClass.UPPERCASE ) {
            baseEntropy = PasswordUtil.log2(26) + 1;
        }
        if ( !this.isAscending() ) {
            baseEntropy += 1;
        }
        return baseEntropy + PasswordUtil.log2(this.getToken().length());

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#toString()
     */
    @Override
    public String toString () {
        return super.toString() + ( this.ascending ? " ASC" : //$NON-NLS-1$ 
                " DESC" ); //$NON-NLS-1$
    }
}
