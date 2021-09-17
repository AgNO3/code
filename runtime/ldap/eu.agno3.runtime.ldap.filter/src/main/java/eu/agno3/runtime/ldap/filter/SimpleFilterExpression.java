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
public class SimpleFilterExpression extends AbstractAttrFilterExpression {

    private String value;
    private FilterType type;


    /**
     * @param attr
     * @param type
     * @param value
     */
    public SimpleFilterExpression ( String attr, FilterType type, String value ) {
        super(attr);
        this.type = type;
        this.value = value;
    }


    /**
     * @return the value
     */
    public String getValue () {
        return this.value;
    }


    /**
     * @return the type
     */
    public FilterType getType () {
        return this.type;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.FilterExpression#getSourceLength()
     */
    @Override
    public int getSourceLength () {
        return super.getSourceLength() + this.type.getSymbol().length() + FilterEscaping.escape(this.value).length() + 2;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof SimpleFilterExpression ) {
            SimpleFilterExpression s = (SimpleFilterExpression) obj;
            return s.getAttr().equals(this.getAttr()) && // attr
                    s.getType().equals(this.getType()) && // type
                    s.getValue().equals(this.getValue()); // value
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
        return this.getAttr().hashCode() + this.getType().hashCode() + this.getValue().hashCode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        StringBuilder b = new StringBuilder();
        b.append('(');
        b.append(FilterEscaping.escape(this.getAttr()));
        b.append(this.getType().getSymbol());
        b.append(FilterEscaping.escape(this.getValue()));
        b.append(')');
        return b.toString();
    }
}
