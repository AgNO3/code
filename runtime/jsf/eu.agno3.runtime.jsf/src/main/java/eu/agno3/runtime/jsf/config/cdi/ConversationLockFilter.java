package eu.agno3.runtime.jsf.config.cdi;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * Chuck Norris can handle requests to busy conversations.
 */
@WebListener
public class ConversationLockFilter implements Filter, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7998251985279739218L;
    /**
     * 
     */
    private static final String CDI_CONVERSATION_LOCKS = "cdi.conversation.locks"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ConversationLockFilter.class);
    private static final long DEFAULT_TIMEOUT = 10;
    private long timeoutSeconds = DEFAULT_TIMEOUT;


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {}


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig cfg ) throws ServletException {}


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpSession session = httpReq.getSession(false);
        if ( session == null ) {
            chain.doFilter(req, resp);
            return;
        }

        String cid = req.getParameter("cid"); //$NON-NLS-1$
        if ( StringUtils.isBlank(cid) ) {
            chain.doFilter(req, resp);
            return;
        }

        Map<String, DebugableReentrantLock> locks = getConversationLocks(session);
        long start = log.isDebugEnabled() ? System.currentTimeMillis() : 0;

        ReentrantLock l = null;
        try {
            l = doLock(cid, locks);
            chain.doFilter(req, resp);
        }
        catch ( InterruptedException e ) {
            throw new ServletException("Interrupted waiting for converstation lock", e); //$NON-NLS-1$
        }
        finally {
            if ( l != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Unlocking conversation %s after %d ms", cid, System.currentTimeMillis() - start)); //$NON-NLS-1$
                    log.debug(httpReq.getRequestURI());
                }
                l.unlock();
            }
        }
    }


    /**
     * @param cid
     * @param locks
     * @return
     * @throws InterruptedException
     * @throws ServletException
     */
    private DebugableReentrantLock doLock ( String cid, Map<String, DebugableReentrantLock> locks ) throws InterruptedException, ServletException {
        DebugableReentrantLock l;
        if ( log.isDebugEnabled() ) {
            log.debug("Locking conversation " + cid); //$NON-NLS-1$
        }
        synchronized ( locks ) {
            l = locks.get(cid);
            if ( l == null ) {
                log.debug("Creating new lock"); //$NON-NLS-1$
                l = new DebugableReentrantLock();
                l.lock();
                locks.put(cid, l);
            }
            else {
                if ( !l.tryLock(this.timeoutSeconds, TimeUnit.SECONDS) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Conversion lock held by " + l.getOwner()); //$NON-NLS-1$
                    }
                    throw new ServletException("Could not get lock for conversation " + cid); //$NON-NLS-1$
                }
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Locked conversation " + cid); //$NON-NLS-1$
        }
        return l;
    }


    /**
     * @param session
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private static Map<String, DebugableReentrantLock> getConversationLocks ( HttpSession session ) {
        Map<String, DebugableReentrantLock> locks;
        synchronized ( session ) {
            locks = (Map<String, DebugableReentrantLock>) session.getAttribute(CDI_CONVERSATION_LOCKS);
            if ( locks == null ) {
                log.debug("Creating new lock map"); //$NON-NLS-1$
                locks = new HashMap<>();
                session.setAttribute(CDI_CONVERSATION_LOCKS, locks);
            }
        }
        return locks;
    }
}