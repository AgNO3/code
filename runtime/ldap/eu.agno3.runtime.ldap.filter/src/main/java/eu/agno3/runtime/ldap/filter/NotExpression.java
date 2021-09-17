/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


/**
 * @author mbechler
 * 
 */
public class NotExpression implements FilterExpression {

    private FilterExpression negated;


    /**
     * @param negated
     */
    public NotExpression ( FilterExpression negated ) {
        this.negated = negated;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.FilterExpression#getSourceLength()
     */
    @Override
    public int getSourceLength () {
        int length = 3;
        return length + this.negated.getSourceLength();
    }


    /**
     * @return the negated expression
     */
    public FilterExpression getNegated () {
        return this.negated;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof NotExpression ) {
            return this.getNegated().equals( ( (NotExpression) obj ).getNegated());
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("(!%s)", this.getNegated().toString()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return Integer.MAX_VALUE - this.getNegated().hashCode();
    }

}
