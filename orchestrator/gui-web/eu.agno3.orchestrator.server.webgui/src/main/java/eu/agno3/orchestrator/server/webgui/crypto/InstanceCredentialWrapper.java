/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 21, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto;


import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.InstanceService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.component.auth.AuthConstants;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.security.credentials.UnwrappedCredentials;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class InstanceCredentialWrapper {

    private static final Logger log = Logger.getLogger(InstanceCredentialWrapper.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    @OsgiService ( dynamic = true )
    private TrustChecker trustChecker;

    @Inject
    @OsgiService ( dynamic = true, filter = "(instanceId=internal)" )
    private TrustConfiguration trustConfig;


    public WrappedCredentials wrap ( InstanceStructuralObject instance, UnwrappedCredentials creds )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        AgentInfo agentInfo = this.ssp.getService(InstanceService.class).getAgentInfo(instance);
        if ( agentInfo == null || agentInfo.getCertificate() == null ) {
            throw new IllegalStateException("Don't have agent certificate"); //$NON-NLS-1$
        }

        X509Certificate agentCert = agentInfo.getCertificate();

        if ( log.isDebugEnabled() ) {
            log.debug("Have target certificate " + agentCert); //$NON-NLS-1$
        }

        try {
            // check that the certificate is valid for an agent
            // we cannot really check that this is in fact for the target agent as the mapping is done by the server
            // main purpose of this encryption is to mitigate against passive attacks, e.g. recovering them from some
            // accidentially persisted block.
            // to prevent active attacks by a rouge server we would need some pinning of agent keys to instances
            this.trustChecker.validate(this.trustConfig, agentCert, Collections.EMPTY_SET, new Date(), null, null);
            X500Name name = X500Name.getInstance(agentCert.getSubjectX500Principal().getEncoded());
            RDN[] rdNs = name.getRDNs(AuthConstants.AGENT_ID_OID);
            if ( rdNs == null || rdNs.length != 1 || ! ( rdNs[ 0 ].getFirst().getValue() instanceof ASN1String ) ) {
                throw new ModelServiceException("Invalid agent certificate"); //$NON-NLS-1$
            }
            return this.csp.getCredentialWrapper().wrap(creds, agentCert.getPublicKey());
        }
        catch (
            CryptoException |
            IOException e ) {
            throw new ModelServiceException("Failed to wrap credentials", e); //$NON-NLS-1$
        }
    }

}
