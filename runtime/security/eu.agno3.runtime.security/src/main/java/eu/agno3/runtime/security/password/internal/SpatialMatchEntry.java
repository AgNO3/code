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
public class SpatialMatchEntry extends MatchEntry {

    private final KeyboardGraph graph;
    private final int turns;
    private final int shiftedCount;


    /**
     * @param token
     * @param startPos
     * @param graph
     * @param turns
     * @param shiftedCount
     */
    public SpatialMatchEntry ( String token, int startPos, KeyboardGraph graph, int turns, int shiftedCount ) {
        super(token, startPos);
        this.graph = graph;
        this.turns = turns;
        this.shiftedCount = shiftedCount;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#getType()
     */
    @Override
    public MatchType getType () {
        return MatchType.SPATIAL;
    }


    /**
     * @return the turns
     */
    public int getTurns () {
        return this.turns;
    }


    /**
     * @return the number of shifted characters
     */
    public int getShiftedCount () {
        return this.shiftedCount;
    }


    /**
     * @return the keyboard graph
     */
    public KeyboardGraph getGraph () {
        return this.graph;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#calcEntropy()
     */
    @Override
    protected float calcEntropy () {
        int possibilities = calcTurnPossibilities();
        float entropy = PasswordUtil.log2(possibilities);
        if ( getShiftedCount() > 0 ) {
            int shiftedPossibilities = calcShiftPossibilities();
            entropy += PasswordUtil.log2(shiftedPossibilities);
        }
        return entropy;
    }


    /**
     * @return
     */
    int calcShiftPossibilities () {
        int unshiftedCount = this.getToken().length() - getShiftedCount();
        int shiftedPossibilities = PasswordUtil.getPossibilities(this.getShiftedCount(), unshiftedCount);
        return shiftedPossibilities;
    }


    /**
     * @return
     */
    int calcTurnPossibilities () {
        int possibilities = 0;
        for ( int pos = 2; pos <= this.getToken().length(); pos++ ) {
            int possibleTurns = Math.min(this.getTurns(), pos - 1);
            for ( int j = 1; j <= possibleTurns; j++ ) {
                possibilities += PasswordUtil.nChooseK(pos - 1, j - 1) * this.getGraph().getStartingPositions()
                        * Math.pow(this.getGraph().getAverageDegree(), j);
            }
        }
        return possibilities;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.MatchEntry#toString()
     */
    @Override
    public String toString () {
        return String.format("%s shifted %d turns %d graph %s", super.toString(), this.shiftedCount, this.turns, this.graph.getType()); //$NON-NLS-1$
    }

}
