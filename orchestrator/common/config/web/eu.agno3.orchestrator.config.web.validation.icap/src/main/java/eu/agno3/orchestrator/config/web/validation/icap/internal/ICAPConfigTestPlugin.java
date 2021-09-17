/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.icap.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.ICAPConfiguration;
import eu.agno3.orchestrator.config.web.validation.SSLEndpointConfigTestFactory;
import eu.agno3.orchestrator.config.web.validation.SocketValidationUtils;
import eu.agno3.orchestrator.config.web.validation.TLSTestContext;
import eu.agno3.orchestrator.config.web.validation.TLSValidationUtils;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPConnectionFactory;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPProtocolException;
import eu.agno3.runtime.net.icap.ICAPProtocolStatusException;
import eu.agno3.runtime.net.icap.ICAPScanRequest;
import eu.agno3.runtime.net.icap.ICAPScannerException;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestPlugin.class )
public class ICAPConfigTestPlugin implements ConfigTestPluginAsync<ICAPConfiguration> {

    private static final Logger log = Logger.getLogger(ICAPConfigTestPlugin.class);

    private static final String EICAR = "WDVPIVAlQEFQWzRcUFpYNTQoUF4pN0NDKTd9JEVJQ0FSLVNUQU5EQVJELUFOVElWSVJVUy1URVNU" + //$NON-NLS-1$
            "LUZJTEUhJEgrSCoK"; //$NON-NLS-1$

    private static final String EICAR_GZ = "H4sICAWgf1gAA2VpY2FyLnR4dACLMPVXDFB1cAyINokJiIowNdEIiNM0d3bWNK9VcfV0dgzSDQ5x" + //$NON-NLS-1$
            "9HNxDHLRdfQL8QzzDAoN1g1xDQ7RdfP0cVVU8dD20OICANsr0B1FAAAA"; //$NON-NLS-1$

    private ICAPConnectionFactory icapConnFactory;

    private SSLEndpointConfigTestFactory sslEndpointFactory;


    @Reference
    protected synchronized void setConnectionFactory ( ICAPConnectionFactory cf ) {
        this.icapConnFactory = cf;
    }


    protected synchronized void unsetConnectionFactory ( ICAPConnectionFactory cf ) {
        if ( this.icapConnFactory == cf ) {
            this.icapConnFactory = null;
        }
    }


    @Reference
    protected synchronized void setSSLEndpointFactory ( SSLEndpointConfigTestFactory sectf ) {
        this.sslEndpointFactory = sectf;
    }


    protected synchronized void unsetSSLEndpointFactory ( SSLEndpointConfigTestFactory sectf ) {
        if ( this.sslEndpointFactory == sectf ) {
            this.sslEndpointFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<ICAPConfiguration> getTargetType () {
        return ICAPConfiguration.class;
    }


    /**
     * <
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getRunOn()
     */
    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return Collections.singleton(ConfigTestPluginRunOn.SERVER);
    }


    private static List<ICAPScanRequest> getCleanPayloads () {
        List<ICAPScanRequest> requests = new ArrayList<>();

        ICAPScanRequest test = new ICAPScanRequest(new ByteArrayInputStream("test".getBytes(StandardCharsets.US_ASCII))); //$NON-NLS-1$
        test.setFileName("clean-test.txt"); //$NON-NLS-1$
        requests.add(test);

        ICAPScanRequest testExe = new ICAPScanRequest(new ByteArrayInputStream("test".getBytes(StandardCharsets.US_ASCII))); //$NON-NLS-1$
        testExe.setFileName("clean.exe"); //$NON-NLS-1$
        requests.add(testExe);

        return requests;

    }


    private static List<ICAPScanRequest> getDirtyPayloads () {
        List<ICAPScanRequest> requests = new ArrayList<>();

        ICAPScanRequest eicar = new ICAPScanRequest(new ByteArrayInputStream(Base64.getDecoder().decode(EICAR)));
        eicar.setFileName("eicar.com"); //$NON-NLS-1$
        requests.add(eicar);

        ICAPScanRequest eicartxt = new ICAPScanRequest(new ByteArrayInputStream(Base64.getDecoder().decode(EICAR)));
        eicartxt.setFileName("eicar.txt"); //$NON-NLS-1$
        eicartxt.setContentType("text/plain"); //$NON-NLS-1$
        requests.add(eicartxt);

        ICAPScanRequest eicarGz = new ICAPScanRequest(new ByteArrayInputStream(Base64.getDecoder().decode(EICAR_GZ)));
        eicarGz.setFileName("eic-gz"); //$NON-NLS-1$
        requests.add(eicarGz);

        return requests;
    }


    @Override
    public ConfigTestResult testAsync ( ICAPConfiguration config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {
        log.debug("Running ICAP test"); //$NON-NLS-1$

        Random rand = new Random();

        URI server = config.getServers().get(rand.nextInt(config.getServers().size()));
        String did = server.toString();

        TLSTestContext tc;
        try {
            tc = this.sslEndpointFactory.adaptSSLClient(config.getSslClientConfiguration());
            TLSValidationUtils.checkTruststoreUsage(tc, r);
        }
        catch ( CryptoException e ) {
            log.debug("Failed to create ssl parameters", e); //$NON-NLS-1$
            r.error("SSL_CONFIG", did, e.getMessage()); //$NON-NLS-1$
            return null;
        }

        r.info("ICAP_CONNECTING", did); //$NON-NLS-1$
        h.update(r);
        try ( ICAPConnection c = this.icapConnFactory.createICAPConnection(new ICAPConfigurationAdapter(config, server), tc.getContext()) ) {
            r.info("ICAP_CONNECTED", did); //$NON-NLS-1$
            h.update(r);

            for ( ICAPScanRequest req : getCleanPayloads() ) {
                doScanFile(r, h, c, req, false);
            }

            for ( ICAPScanRequest req : getDirtyPayloads() ) {
                doScanFile(r, h, c, req, true);
            }

            return r.state(ConfigTestState.SUCCESS);
        }
        catch ( IOException e ) {
            SocketValidationUtils.handleIOException(e, r, did);
        }
        catch ( ICAPException e ) {

            if ( e.getCause() instanceof IOException ) {
                SocketValidationUtils.handleIOException((IOException) e.getCause(), r, did);
            }
            else if ( e instanceof ICAPProtocolException ) {
                handleProtocolException(e, r, server);
            }
            else {
                log.debug("Unknown error", e); //$NON-NLS-1$
                r.error("ICAP_UNKNOWN_FAIL", e.getMessage()); //$NON-NLS-1$
            }
        }

        return r.state(ConfigTestState.FAILURE);
    }


    /**
     * @param r
     * @param h
     * @param c
     * @param isr
     * @param expectFail
     * @throws ICAPException
     */
    private static void doScanFile ( ConfigTestResult r, ConfigTestAsyncHandler h, ICAPConnection c, ICAPScanRequest isr, boolean expectFail )
            throws ICAPException {
        try {
            c.scan(isr);

            if ( expectFail ) {
                r.warn("ICAP_EXPECT_REJECT", isr.getFileName()); //$NON-NLS-1$
            }
            else {
                r.info("ICAP_NOT_REJECTED", isr.getFileName()); //$NON-NLS-1$
            }
        }
        catch ( ICAPException e ) {
            if ( e.getCause() instanceof ICAPException ) {
                throw (ICAPException) e.getCause();
            }
            throw e;
        }
        catch ( ICAPScannerException e ) {
            log.debug("Scan failed", e); //$NON-NLS-1$

            if ( e.getCause() instanceof ICAPException ) {
                throw (ICAPException) e.getCause();
            }

            if ( !expectFail ) {
                r.warn("ICAP_REJECTED", isr.getFileName(), e.getSignature()); //$NON-NLS-1$
            }
            else {
                r.info("ICAP_EXPECT_REJECT_OK", isr.getFileName(), e.getSignature()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param e
     * @param r
     * @param server
     */
    void handleProtocolException ( ICAPException e, ConfigTestResult r, URI server ) {
        if ( e instanceof ICAPProtocolStatusException ) {
            if ( ( (ICAPProtocolStatusException) e ).getStatusCode() == 404 ) {
                r.error("ICAP_STATUS_404", e.getMessage(), server.getPath()); //$NON-NLS-1$
            }
            else {
                r.error("ICAP_STATUS_FAIL", e.getMessage()); //$NON-NLS-1$
            }
        }
        else {
            r.error("ICAP_PROTOCOL_FAIL", e.getMessage()); //$NON-NLS-1$
        }
    }

}
