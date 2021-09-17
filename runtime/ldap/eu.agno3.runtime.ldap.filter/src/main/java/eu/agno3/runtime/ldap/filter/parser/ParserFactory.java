/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter.parser;


import eu.agno3.runtime.ldap.filter.FilterParserException;


/**
 * @author mbechler
 * 
 */
public interface ParserFactory {

    /**
     * @param filterSpec
     * @return A parsed filter expression
     * @throws FilterParserException
     *             if filter parsing fails
     */
    Parser parseString ( String filterSpec ) throws FilterParserException;

}
