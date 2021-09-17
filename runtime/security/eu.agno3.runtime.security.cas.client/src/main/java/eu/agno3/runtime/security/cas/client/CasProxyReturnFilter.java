/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.PathMatchingFilter;


/**
 * @author mbechler
 *
 */
public class CasProxyReturnFilter extends PathMatchingFilter {

    private static final String PGT_IOU = "pgtIou"; //$NON-NLS-1$
    private static final String PGT_ID = "pgtId"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(CasProxyReturnFilter.class);

    private AbstractCasRealm casProxyRealm;


    /**
     * @param casProxyRealm
     */
    public CasProxyReturnFilter ( AbstractCasRealm casProxyRealm ) {
        this.casProxyRealm = casProxyRealm;
        setName("casProxyReturnFilter"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.servlet.AdviceFilter#doFilterInternal(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws ServletException, IOException {
        log.trace("CasProxyReturnFilter called"); //$NON-NLS-1$

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        String[] pgtIds = httpReq.getParameterValues(PGT_ID);
        String[] pgtIous = httpReq.getParameterValues(PGT_IOU);

        if ( pgtIds == null || pgtIous == null || pgtIds.length != 1 || pgtIous.length != 1 ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Recieved proxy callback without data"); //$NON-NLS-1$
            }
            httpResp.setStatus(200);
            return;
        }

        String pgtId = pgtIds[ 0 ];
        String pgtIou = pgtIous[ 0 ];

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Got PGT ID %s IOU %s", pgtId, pgtIou)); //$NON-NLS-1$
        }

        this.casProxyRealm.handleProxyReturn(pgtId, pgtIou);
        httpResp.setStatus(200);
    }
}
