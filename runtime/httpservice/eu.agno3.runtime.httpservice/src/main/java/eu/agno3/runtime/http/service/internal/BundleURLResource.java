/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.jetty.util.resource.URLResource;


/**
 * @author mbechler
 * 
 */
public class BundleURLResource extends URLResource {

    /**
     * @param url
     * @param connection
     */
    protected BundleURLResource ( URL url, URLConnection connection ) {
        super(url, connection);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.resource.URLResource#getFile()
     */
    @Override
    public File getFile () throws IOException {
        return null;
    }
}
