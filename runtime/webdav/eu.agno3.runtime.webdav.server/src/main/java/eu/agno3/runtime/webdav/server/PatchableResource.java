/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.io.IOException;
import java.util.Collection;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.io.InputContext;


/**
 * @author mbechler
 *
 */
public interface PatchableResource extends DavResource {

    /**
     * 
     */
    public static final String METHODS = "PATCH"; //$NON-NLS-1$


    /**
     * @return acceptable patch formats
     */
    Collection<String> getAcceptablePatchFormats ();


    /**
     * @param resource
     * @param inputContext
     * @throws DavException
     * @throws IOException
     */
    void patch ( InputContext inputContext ) throws DavException, IOException;

}
