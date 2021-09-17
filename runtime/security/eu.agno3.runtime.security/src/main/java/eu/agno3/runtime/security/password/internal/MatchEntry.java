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
public class MatchEntry {

    private static final float YEAR_ENTROPY = PasswordUtil.log2(119);

    private final MatchType type;

    private final int startPos;

    private final int endPos;

    private Float cachedEntropy;

    private final String token;


    /**
     * 
     * @param token
     * @param startPos
     */
    public MatchEntry ( String token, int startPos ) {
        this(token, startPos, null);
    }


    /**
     * @param token
     * @param startPos
     * @param type
     * 
     */
    public MatchEntry ( String token, int startPos, MatchType type ) {
        this.token = token;
        this.startPos = startPos;
        this.type = type;
        this.endPos = this.startPos + this.token.length();
    }


    /**
     * @return the type
     */
    public MatchType getType () {
        return this.type;
    }


    /**
     * @return the startPos
     */
    public int getStartPos () {
        return this.startPos;
    }


    /**
     * @return the endPos
     */
    public int getEndPos () {
        return this.endPos;
    }


    /**
     * @return the matched token
     */
    public String getToken () {
        return this.token;
    }


    /**
     * 
     * @return the token entropy
     */
    public float getEntropy () {
        if ( this.cachedEntropy == null ) {
            this.cachedEntropy = this.calcEntropy();
        }

        return this.cachedEntropy;
    }


    /**
     * 
     * @return the entropy
     */
    protected float calcEntropy () {
        switch ( this.getType() ) {
        case DIGITS:
            return calcDigitEntropy(this);
        case REPEAT:
            return calcRepeatEntropy(this);
        case YEAR:
            return YEAR_ENTROPY;
        default:
            throw new IllegalArgumentException();
        }
    }


    /**
     * @param e
     * @return
     */
    private static float calcRepeatEntropy ( MatchEntry e ) {
        return PasswordUtil.log2(PasswordUtil.getBruteforceCardinality(e.getToken()) * e.getToken().length());
    }


    /**
     * @param e
     * @return
     */
    private static float calcDigitEntropy ( MatchEntry e ) {
        return e.getToken().length() * PasswordUtil.log2(10);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s [%d:%d] %.2f", this.getType(), this.getStartPos(), this.getEndPos(), this.getEntropy()); //$NON-NLS-1$
    }
}
