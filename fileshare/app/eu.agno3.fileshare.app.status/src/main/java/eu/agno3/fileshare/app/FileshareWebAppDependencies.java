/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.fileshare.app;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.webdav.FileshareWebDAVServlet;
import eu.agno3.runtime.http.service.webapp.WebAppDependencies;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
@Component ( service = WebAppDependencies.class, property = "instanceId=fileshare" )
public class FileshareWebAppDependencies implements WebAppDependencies {

    @Reference
    protected synchronized void bindServiceContext ( DefaultServiceContext dsc ) {}


    protected synchronized void unbindServiceContext ( DefaultServiceContext dsc ) {}


    @Reference
    protected synchronized void bindWebDAVServlet ( FileshareWebDAVServlet fws ) {}


    protected synchronized void unbindWebDAVServlet ( FileshareWebDAVServlet fws ) {}
}
