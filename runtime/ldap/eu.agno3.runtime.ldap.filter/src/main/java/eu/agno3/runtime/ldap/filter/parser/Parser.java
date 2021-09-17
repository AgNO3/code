/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter.parser;

import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * 
 */
public interface Parser {

    /**
     * @return the parsed expression
     */
    FilterExpression getExpression ();

}
