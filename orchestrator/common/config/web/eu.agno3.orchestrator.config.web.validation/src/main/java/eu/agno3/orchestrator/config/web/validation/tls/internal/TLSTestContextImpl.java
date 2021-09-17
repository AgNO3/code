/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.tls.internal;


import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import eu.agno3.orchestrator.config.web.validation.TLSTestContext;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class TLSTestContextImpl implements TLSTestContext {

    private TLSContext context;
    private TestingTrustManager tm;
    private TestingHostnameVerifier hv;
    private boolean trustFallback;
    private String tmFallbackFrom;
    private String tmFallbackTo;


    /**
     * @param context
     * @param tm
     * @param hv
     */
    public TLSTestContextImpl ( TLSContext context, TestingTrustManager tm, TestingHostnameVerifier hv ) {
        this.context = context;
        this.tm = tm;
        this.hv = hv;
        this.trustFallback = false;
    }


    /**
     * @param context
     * @param tm
     * @param hv
     * @param tmFallbackFrom
     * @param tmFallbackTo
     */
    public TLSTestContextImpl ( TLSContext context, TestingTrustManager tm, TestingHostnameVerifier hv, String tmFallbackFrom, String tmFallbackTo ) {
        this.context = context;
        this.tm = tm;
        this.hv = hv;
        this.tmFallbackFrom = tmFallbackFrom;
        this.tmFallbackTo = tmFallbackTo;
        this.trustFallback = true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.validation.TLSTestContext#getContext()
     */
    @Override
    public TLSContext getContext () {
        return this.context;
    }


    @Override
    public Map<String, X509Certificate[]> getNameValidationFailures () {
        return this.hv.getFailed();
    }


    @Override
    public List<X509Certificate[]> getChainValidationFailures () {
        return this.tm.getFailed();
    }


    @Override
    public boolean isTrustFallback () {
        return this.trustFallback;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.validation.TLSTestContext#getTrustFallbackFrom()
     */
    @Override
    public String getTrustFallbackFrom () {
        return this.tmFallbackFrom;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.validation.TLSTestContext#getTrustFallbackTo()
     */
    @Override
    public String getTrustFallbackTo () {
        return this.tmFallbackTo;
    }
}
