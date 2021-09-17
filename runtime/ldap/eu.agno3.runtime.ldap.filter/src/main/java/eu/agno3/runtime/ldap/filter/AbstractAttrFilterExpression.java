/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractAttrFilterExpression extends AbstractItemExpression {

    private String attr;


    /**
     * @param attr
     * 
     */
    public AbstractAttrFilterExpression ( String attr ) {
        super();
        this.attr = attr;
    }


    /**
     * @return the attr
     */
    public String getAttr () {
        return this.attr;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.AbstractItemExpression#getSourceLength()
     */
    @Override
    public int getSourceLength () {
        return FilterEscaping.escape(this.attr).length();
    }
}