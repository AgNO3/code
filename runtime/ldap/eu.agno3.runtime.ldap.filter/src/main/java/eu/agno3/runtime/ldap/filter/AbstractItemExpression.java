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
public abstract class AbstractItemExpression implements FilterExpression {

    protected AbstractItemExpression () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.FilterExpression#getSourceLength()
     */
    @Override
    public abstract int getSourceLength ();
}
