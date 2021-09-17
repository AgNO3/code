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
public class DateMatchEntry extends MatchEntry {

    private static final int NUM_DAYS = 31;
    private static final int NUM_MONTHS_ = 12;
    private static final int NUM_YEARS = 119;
    private static final float DATE_2_ENTROPY = PasswordUtil.log2(NUM_DAYS * NUM_MONTHS_ * 100);
    private static final float DATE_4_ENTROPY = PasswordUtil.log2(NUM_DAYS * NUM_MONTHS_ * NUM_YEARS);

    private final int year;
    private final boolean separators;


    /**
     * @param token
     * @param startPos
     * @param year
     * @param separators
     */
    public DateMatchEntry ( String token, int startPos, int year, boolean separators ) {
        super(token, startPos);
        this.year = year;
        this.separators = separators;
    }


    /**
     * @return the year
     */
    public int getYear () {
        return this.year;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getType()
     */
    @Override
    public MatchType getType () {
        return MatchType.DATE;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#calcEntropy()
     */
    @Override
    protected float calcEntropy () {
        float entropy = 0;
        if ( this.year < 100 ) {
            entropy = DATE_2_ENTROPY;
        }
        else {
            entropy = DATE_4_ENTROPY;
        }

        if ( this.separators ) {
            entropy += 2;
        }

        return entropy;
    }
}
