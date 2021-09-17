/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.05.2015 by mbechler
 */
package eu.agno3.runtime.http.service.session;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class SessionSizeTraceFilter implements Filter {

    private static final Logger log = Logger.getLogger(SessionSizeTraceFilter.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {

        if ( !log.isDebugEnabled() || ! ( req instanceof HttpServletRequest ) ) {
            chain.doFilter(req, resp);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpSession session = httpReq.getSession(false);
        if ( session == null ) {
            chain.doFilter(req, resp);
            return;
        }

        long beforeSessionSize = getSerializedSessionSize(session);
        chain.doFilter(req, resp);
        long afterSessionSize = getSerializedSessionSize(session);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Session size before: %d kb after: %d kb", beforeSessionSize / 1024, afterSessionSize / 1024)); //$NON-NLS-1$
        }

    }


    /**
     * @param session
     * @return
     * @throws IOException
     */
    @SuppressWarnings ( "unchecked" )
    private static long getSerializedSessionSize ( HttpSession session ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long curSize = 0;
        try ( ObjectOutputStream oos = new ObjectOutputStream(bos) ) {
            Enumeration<String> attrs = session.getAttributeNames();
            while ( attrs.hasMoreElements() ) {
                String attr = attrs.nextElement();
                try {
                    Object attribute = session.getAttribute(attr);

                    if ( attribute instanceof Map ) {
                        for ( Entry<Object, Object> e : ( (Map<Object, Object>) attribute ).entrySet() ) {
                            oos.writeObject(e.getKey());
                            oos.writeObject(e.getValue());
                            curSize = traceElementSize(bos, curSize, oos, attr + "/" + e.getKey()); //$NON-NLS-1$
                        }

                    }
                    else {
                        oos.writeObject(attribute);
                        curSize = traceElementSize(bos, curSize, oos, attr);
                    }
                }
                catch ( NotSerializableException e ) {
                    log.warn("Session contains non serializable data", e); //$NON-NLS-1$
                }

            }

        }
        catch (
            IllegalStateException |
            IOException e ) {
            log.trace("Failed to get session size", e); //$NON-NLS-1$
        }
        return bos.size();
    }


    /**
     * @param bos
     * @param curSize
     * @param oos
     * @param attr
     * @return
     * @throws IOException
     */
    private static long traceElementSize ( ByteArrayOutputStream bos, long curSize, ObjectOutputStream oos, String attr ) throws IOException {
        if ( log.isTraceEnabled() ) {
            oos.flush();
            long diff = bos.size() - curSize;
            log.trace(String.format("Key %s : %d kb", attr, diff / 1024)); //$NON-NLS-1$
        }
        return bos.size();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig cfg ) throws ServletException {

    }

}
