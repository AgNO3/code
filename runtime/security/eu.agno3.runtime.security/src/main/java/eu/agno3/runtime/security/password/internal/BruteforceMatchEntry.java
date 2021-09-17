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
public class BruteforceMatchEntry extends MatchEntry {

    private float entropy;
    private int bruteforceCardinality;


    /**
     * @param token
     * @param startPos
     * @param entropy
     * @param bruteforceCardinality
     */
    public BruteforceMatchEntry ( String token, int startPos, float entropy, int bruteforceCardinality ) {
        super(token, startPos);
        this.entropy = entropy;
        this.bruteforceCardinality = bruteforceCardinality;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getType()
     */
    @Override
    public MatchType getType () {
        return MatchType.BRUTEFORCE;
    }


    /**
     * @return the bruteforceCardinality
     */
    public int getBruteforceCardinality () {
        return this.bruteforceCardinality;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getEntropy()
     */
    @Override
    public float getEntropy () {
        return this.entropy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#toString()
     */
    @Override
    public String toString () {
        return super.toString() + " card " + this.bruteforceCardinality; //$NON-NLS-1$
    }
}
