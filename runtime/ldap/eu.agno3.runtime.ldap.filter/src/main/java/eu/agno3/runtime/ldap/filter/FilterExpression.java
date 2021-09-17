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
public interface FilterExpression {

    /**
     * Get the string length of this expression in the source filter
     * 
     * @return expression length
     */
    int getSourceLength ();
}
