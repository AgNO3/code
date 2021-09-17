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
public class ExistsFilterExpression extends AbstractAttrFilterExpression {

    private static final String STRING = "*"; //$NON-NLS-1$


    /**
     * @param attr
     */
    public ExistsFilterExpression ( String attr ) {
        super(attr);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.AbstractAttrFilterExpression#getSourceLength()
     */
    @Override
    public int getSourceLength () {
        return super.getSourceLength() + FilterType.EQUALS.getSymbol().length();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof ExistsFilterExpression ) {
            ExistsFilterExpression s = (ExistsFilterExpression) obj;
            return s.getAttr().equals(this.getAttr());
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
        return this.getAttr().hashCode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("(%s%s%s)", FilterEscaping.escape(this.getAttr()), FilterType.EQUALS.getSymbol(), STRING); //$NON-NLS-1$
    }

}
