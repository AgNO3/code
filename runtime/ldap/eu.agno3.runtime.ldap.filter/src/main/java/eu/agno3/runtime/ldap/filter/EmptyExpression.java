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
public class EmptyExpression implements FilterExpression {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.FilterExpression#getSourceLength()
     */
    @Override
    public int getSourceLength () {
        return 2;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof EmptyExpression ) {
            return true;
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "()"; //$NON-NLS-1$
    }

}
