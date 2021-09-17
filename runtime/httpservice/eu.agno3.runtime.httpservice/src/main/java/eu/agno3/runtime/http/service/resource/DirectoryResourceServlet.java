/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;


/**
 * @author mbechler
 * 
 */
public class DirectoryResourceServlet extends DefaultServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -3487290654949054906L;
    private static final Logger log = Logger.getLogger(DirectoryResourceServlet.class);

    private File baseDir;
    private String baseDirCanonical;


    protected DirectoryResourceServlet ( File baseDir ) throws IOException {
        this.baseDir = baseDir;
        this.baseDirCanonical = baseDir.getCanonicalPath();
    }


    @Override
    public Resource getResource ( String path ) {
        log.debug(String.format("Trying to serve %s using DirectoryResourceServlet on path %s", path, this.baseDir.toString())); //$NON-NLS-1$

        File f = new File(this.baseDir, path);

        try {
            if ( !f.getCanonicalPath().startsWith(this.baseDirCanonical) ) {
                log.error("Prevented directory traversal using path " + path); //$NON-NLS-1$
                return null;
            }
        }
        catch ( IOException e ) {
            log.error("Failed to determine canonical path:", e); //$NON-NLS-1$
        }

        return Resource.newResource(f);
    }

}
