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
public enum FilterType {

    /**
     * Equality comparison
     * 
     */
    EQUALS("="), //$NON-NLS-1$
    /**
     * Approximate equality comparsion
     */
    APPROX("~="), //$NON-NLS-1$
    /**
     * Greater or equal comparsion
     * 
     */
    GREATER_EQUALS(">="), //$NON-NLS-1$
    /**
     * Less than or equal comparsion
     */
    LESS_EQUALS("<="); //$NON-NLS-1$

    private String filterSymbol;


    FilterType ( String filterSymbol ) {
        this.filterSymbol = filterSymbol;
    }


    /**
     * @return the symbol used by this filter
     * 
     */
    public String getSymbol () {
        return this.filterSymbol;
    }


    /**
     * @param comp
     * @return filter type for operator symbol
     */
    public static FilterType fromSymbol ( String comp ) {
        switch ( comp ) {
        case "=": //$NON-NLS-1$
            return FilterType.EQUALS;
        case ">=": //$NON-NLS-1$
            return FilterType.GREATER_EQUALS;
        case "<=": //$NON-NLS-1$
            return FilterType.LESS_EQUALS;
        case "~=": //$NON-NLS-1$
            return FilterType.APPROX;
        default:
            throw new IllegalArgumentException("Unknown operator symbol: " + comp); //$NON-NLS-1$
        }
    }
}
