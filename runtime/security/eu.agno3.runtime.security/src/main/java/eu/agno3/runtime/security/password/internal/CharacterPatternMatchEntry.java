/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public class CharacterPatternMatchEntry extends MatchEntry {

    private Set<Character> chars;


    /**
     * @param token
     * @param startPos
     * @param chars
     */
    public CharacterPatternMatchEntry ( String token, int startPos, Set<Character> chars ) {
        super(token, startPos);
        this.chars = chars;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getType()
     */
    @Override
    public MatchType getType () {
        return MatchType.CHARPATTERN;
    }


    /**
     * @return the chars
     */
    public Set<Character> getChars () {
        return this.chars;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#calcEntropy()
     */
    @Override
    protected float calcEntropy () {
        return PasswordUtil.log2(this.chars.size()) * this.getToken().length();
    }

}
