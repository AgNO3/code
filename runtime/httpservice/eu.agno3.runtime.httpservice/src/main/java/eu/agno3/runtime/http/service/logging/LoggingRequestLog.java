/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.runtime.http.service.logging;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.AbstractNCSARequestLog;


/**
 * @author mbechler
 * 
 */
public class LoggingRequestLog extends AbstractNCSARequestLog {

    private static final Logger log = Logger.getLogger("eu.agno3.runtime.http.service.request"); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.AbstractNCSARequestLog#isEnabled()
     */
    @Override
    protected boolean isEnabled () {
        return log.isDebugEnabled();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.AbstractNCSARequestLog#write(java.lang.String)
     */
    @Override
    public void write ( String msg ) throws IOException {
        log.debug(msg);
    }

}
