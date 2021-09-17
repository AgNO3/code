/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;


/**
 * @author mbechler
 * 
 */
@Component ( service = ErrorHandler.class, servicefactory = true )
public class DefaultErrorHandler extends ErrorHandler {

    private static final Logger log = Logger.getLogger(DefaultErrorHandler.class);


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        try {
            this.stop();
        }
        catch ( Exception e ) {
            log.error("Failed to stop error handler:", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    public DefaultErrorHandler () {
        this.setShowMessageInTitle(true);
        this.setShowStacks(true);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.handler.ErrorHandler#writeErrorPage(javax.servlet.http.HttpServletRequest,
     *      java.io.Writer, int, java.lang.String, boolean)
     */
    @Override
    protected void writeErrorPage ( HttpServletRequest request, Writer writer, int code, String message, boolean showStacks ) throws IOException {
        super.writeErrorPage(request, writer, code, message, showStacks);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.handler.ErrorHandler#writeErrorPageBody(javax.servlet.http.HttpServletRequest,
     *      java.io.Writer, int, java.lang.String, boolean)
     */
    @Override
    protected void writeErrorPageBody ( HttpServletRequest request, Writer writer, int code, String message, boolean showStacks ) throws IOException {
        String uri = request.getRequestURI();
        writeErrorPageMessage(request, writer, code, message, uri);

        if ( showStacks ) {
            writeErrorPageStacks(request, writer);
        }
    }

}
