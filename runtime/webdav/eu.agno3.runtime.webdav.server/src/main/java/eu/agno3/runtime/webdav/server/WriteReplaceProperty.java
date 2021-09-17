/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.property.DavProperty;


/**
 * @author mbechler
 *
 */
public interface WriteReplaceProperty {

    /**
     * @param locator
     * @param locatorFactory
     * @return the modified property
     */
    DavProperty<?> writeReplaceProperty ( DavResourceLocator locator, DavLocatorFactory locatorFactory );

}