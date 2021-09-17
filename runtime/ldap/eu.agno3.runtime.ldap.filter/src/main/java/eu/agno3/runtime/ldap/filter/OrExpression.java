/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


import java.util.List;


/**
 * @author mbechler
 * 
 */
public class OrExpression extends AbstractComplexExpression {

    /**
     * @param clauses
     */
    public OrExpression ( List<FilterExpression> clauses ) {
        super(clauses, "|"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.AbstractComplexExpression#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof OrExpression ) {
            return super.equals(obj);
        }
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.AbstractComplexExpression#hashCode()
     */
    @Override
    public int hashCode () {
        return 5 * super.hashCode();
    }

}
