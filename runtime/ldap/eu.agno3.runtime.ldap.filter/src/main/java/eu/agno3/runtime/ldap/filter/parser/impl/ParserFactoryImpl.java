/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter.parser.impl;


import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ldap.filter.FilterParserException;
import eu.agno3.runtime.ldap.filter.parser.Parser;
import eu.agno3.runtime.ldap.filter.parser.ParserFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ParserFactory.class
} )
public class ParserFactoryImpl implements ParserFactory {

    /**
     * 
     */
    public ParserFactoryImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.parser.ParserFactory#parseString(java.lang.String)
     */
    @Override
    public Parser parseString ( String filterSpec ) throws FilterParserException {
        ParserImpl impl = new ParserImpl();
        impl.parseString(filterSpec);
        return impl;
    }

}
