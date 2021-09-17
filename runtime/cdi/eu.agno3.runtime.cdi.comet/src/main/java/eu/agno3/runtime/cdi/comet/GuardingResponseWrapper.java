/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.cdi.comet;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Response;


/**
 * @author mbechler
 *
 */
public class GuardingResponseWrapper extends HttpServletResponseWrapper {

    private static final Logger log = Logger.getLogger(GuardingResponseWrapper.class);

    private Response origResponse;

    private HttpChannel channel;

    private long request;


    /**
     * @param response
     */
    public GuardingResponseWrapper ( HttpServletResponse response ) {
        super(response);

        ServletResponse resp = response;
        while ( resp instanceof HttpServletResponseWrapper ) {
            resp = ( (HttpServletResponseWrapper) resp ).getResponse();
        }
        this.origResponse = (Response) resp;
        this.channel = this.origResponse.getHttpChannel();
        this.request = this.channel.getRequests();
    }


    /**
     * @return whether that is the same request as the original one
     */
    public boolean checkSameRequest () {
        return this.request == this.channel.getRequests();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletResponseWrapper#setBufferSize(int)
     */
    @Override
    public void setBufferSize ( int size ) {
        if ( !checkSameRequest() ) {
            log.warn("Calling setBufferSize on released connection"); //$NON-NLS-1$
            return;
        }
        super.setBufferSize(size);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int)
     */
    @Override
    public void sendError ( int sc ) throws IOException {
        if ( !checkSameRequest() ) {
            log.warn("Calling sendError on released connection"); //$NON-NLS-1$
            return;
        }
        super.sendError(sc);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int, java.lang.String)
     */
    @Override
    public void sendError ( int sc, String msg ) throws IOException {
        if ( !checkSameRequest() ) {
            log.warn("Calling sendError on released connection"); //$NON-NLS-1$
            return;
        }
        super.sendError(sc, msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect ( String location ) throws IOException {
        if ( !checkSameRequest() ) {
            log.warn("Calling sendRedirect on released connection"); //$NON-NLS-1$
            return;
        }
        super.sendRedirect(location);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream () throws IOException {
        if ( !checkSameRequest() ) {
            log.warn("Calling getOutputStream on released connection"); //$NON-NLS-1$
            throw new IOException("Already released"); //$NON-NLS-1$
        }
        return super.getOutputStream();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter () throws IOException {
        if ( !checkSameRequest() ) {
            log.warn("Calling getWriter on released connection"); //$NON-NLS-1$
            throw new IOException("Already released"); //$NON-NLS-1$
        }
        return super.getWriter();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer () throws IOException {
        if ( !checkSameRequest() ) {
            log.warn("Calling flushBuffer on released connection " + this.channel); //$NON-NLS-1$
            throw new NullPointerException("Already released"); //$NON-NLS-1$
        }

        super.flushBuffer();
    }

}
