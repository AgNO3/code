/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    GuardHandlerImpl.class, PlatformStateListener.class
} )
public class GuardHandlerImpl extends ContextHandler implements PlatformStateListener {

    /**
     * 
     */
    private static final String CONTAINER_STATUS = "/.container-status"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(GuardHandlerImpl.class);

    /**
     * 
     */
    private static final String LOCAL_IP = "127.0.0.1"; //$NON-NLS-1$
    private PlatformState curState = PlatformState.BOOTING;


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.ContextHandler#doHandle(java.lang.String, org.eclipse.jetty.server.Request,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doHandle ( String target, Request req, HttpServletRequest httpReq, HttpServletResponse resp ) throws IOException, ServletException {
        PlatformState state = this.curState;
        if ( CONTAINER_STATUS.equals(target) ) {
            doHandleStatus(req, httpReq, resp, state);
            return;
        }
        else if ( state == PlatformState.STOPPING
                || ( state != PlatformState.STARTED && state != PlatformState.WARNING && !isLocalRequest(httpReq) ) ) {
            doHandleUnavailable(req, httpReq, resp, state);
        }
        super.doHandle(target, req, httpReq, resp);
    }


    private static boolean isLocalRequest ( HttpServletRequest req ) {
        if ( LOCAL_IP.equals(req.getRemoteAddr()) ) {
            return true;
        }

        if ( req.getLocalAddr().equals(req.getRemoteAddr()) ) {
            return true;
        }

        return false;
    }


    /**
     * @param req
     * @param httpReq
     * @param resp
     * @param state
     */
    private static void doHandleStatus ( Request req, HttpServletRequest httpReq, HttpServletResponse resp, PlatformState state ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Status request " + state); //$NON-NLS-1$
        }

        switch ( state ) {
        case BOOTING:
        case RECONFIGURE:
        case UPDATING:
        case STOPPING:
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            break;

        default:
        case FAILED:
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            break;

        case WARNING:
        case STARTED:
            resp.setStatus(HttpServletResponse.SC_OK);
            break;
        }
        req.setHandled(true);
    }


    /**
     * @param req
     * @param httpReq
     * @param resp
     * @param state
     * @throws IOException
     */
    protected void doHandleUnavailable ( Request req, HttpServletRequest httpReq, HttpServletResponse resp, PlatformState state ) throws IOException {
        resp.setContentType("text/html; charset=utf-8"); //$NON-NLS-1$
        writeUnavailablePage(resp.getWriter(), state);
        resp.setStatus(503);
        req.setHandled(true);
    }


    /**
     * @param writer
     * @param state
     */
    @SuppressWarnings ( "nls" )
    private static void writeUnavailablePage ( PrintWriter writer, PlatformState state ) {
        writer.write("<html>");
        writer.write("<head>");
        writer.write("<title>Service temporarily unavailable</title>");
        writer.write("<meta http-equiv=\"refresh\" content=\"2\" />");
        writer.write("</head>");
        writer.write("<body>");
        writer.write("<p>");
        writer.write("Server is temporary unavailable: " + state.name());
        writer.write("</p>");
        writer.write("</body>");
        writer.write("</html>");
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.PlatformStateListener#stateChanged(eu.agno3.runtime.update.PlatformState)
     */
    @Override
    public void stateChanged ( PlatformState state ) {
        this.curState = state;
    }
}
