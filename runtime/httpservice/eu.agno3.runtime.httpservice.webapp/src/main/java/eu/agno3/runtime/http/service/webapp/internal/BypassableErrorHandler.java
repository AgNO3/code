/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.runtime.http.service.webapp.internal;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;


/**
 * @author mbechler
 *
 */
public class BypassableErrorHandler extends ErrorPageErrorHandler {

    private static final Logger log = Logger.getLogger(BypassableErrorHandler.class);
    private ErrorHandler bypassHandler;


    /**
     * @param bypassHandler
     */
    public BypassableErrorHandler ( ErrorHandler bypassHandler ) {
        this.bypassHandler = bypassHandler;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.ErrorHandler#handle(java.lang.String, org.eclipse.jetty.server.Request,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handle ( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response ) throws IOException {

        if ( request.getAttribute("bypass-error-handler") != null ) { //$NON-NLS-1$
            log.debug("Bypassing default error handler"); //$NON-NLS-1$
            this.bypassHandler.handle(target, baseRequest, request, response);
            return;
        }

        super.handle(target, baseRequest, request, response);
    }
}
