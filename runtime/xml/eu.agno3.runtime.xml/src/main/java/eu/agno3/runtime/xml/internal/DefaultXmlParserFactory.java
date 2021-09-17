/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.internal;


import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.xml.AbstractXmlParserFactoryImpl;
import eu.agno3.runtime.xml.XmlParserFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = XmlParserFactory.class )
public class DefaultXmlParserFactory extends AbstractXmlParserFactoryImpl {

    /**
     */
    public DefaultXmlParserFactory () {
        super(new DefaultParserConfigurator());
    }

}
