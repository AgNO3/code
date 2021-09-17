/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.rules;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext;
import eu.agno3.fileshare.service.policy.internal.PolicyRule;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSCipherSuiteSpec;
import eu.agno3.runtime.crypto.tls.TLSCipherSuiteUtil;
import eu.agno3.runtime.crypto.tls.TLSEncryptionAlgorithm;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyRule.class )
public class TransportSecurityRule implements PolicyRule {

    private static final Logger log = Logger.getLogger(TransportSecurityRule.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#getPriority()
     */
    @Override
    public float getPriority () {
        return -400;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyRule#isFulfilledForAccess(eu.agno3.fileshare.service.config.PolicyConfiguration,
     *      eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext)
     */
    @Override
    public PolicyViolation isFulfilledForAccess ( PolicyConfiguration policyConfig, PolicyEvaluationContext ctx ) {
        HttpServletRequest req = (HttpServletRequest) ctx.getServletRequest();

        String cipherSuite = (String) req.getAttribute("javax.servlet.request.cipher_suite"); //$NON-NLS-1$

        if ( !policyConfig.isTransportRequireEncryption() ) {
            return null;
        }

        if ( StringUtils.isBlank(cipherSuite) ) {
            return new PolicyViolation("transport.missingEncryption"); //$NON-NLS-1$
        }

        TLSCipherSuiteSpec spec;
        try {
            spec = TLSCipherSuiteUtil.parseJavaSpec(cipherSuite);
        }
        catch ( CryptoException e ) {
            log.warn("Failed to parse cipher suite ", e); //$NON-NLS-1$
            return new PolicyViolation("transport.detectionError"); //$NON-NLS-1$
        }

        if ( spec.getEncAlgo() == TLSEncryptionAlgorithm.NULL ) {
            return new PolicyViolation("transport.missingEncryption"); //$NON-NLS-1$
        }

        if ( policyConfig.isTransportRequirePFS() && !spec.getKeyAlgo().isPfs() ) {
            return new PolicyViolation("transport.missingPFS"); //$NON-NLS-1$
        }

        if ( spec.getEncAlgo().getKeySize() < policyConfig.getTransportMinKeySize() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Policy requires key size %d current (%s) has %d", //$NON-NLS-1$
                    policyConfig.getTransportMinKeySize(),
                    cipherSuite,
                    spec.getEncAlgo().getKeySize()));
            }
            return new PolicyViolation("transport.keySize", spec.getEncAlgo().getKeySize(), policyConfig.getTransportMinKeySize()); //$NON-NLS-1$
        }

        if ( spec.getHash().getBlockSize() < policyConfig.getTransportMinHashBlockSize() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Policy requires hash size %d current (%s) has %d", //$NON-NLS-1$
                    policyConfig.getTransportMinHashBlockSize(),
                    cipherSuite,
                    spec.getHash().getBlockSize()));
            }
            return new PolicyViolation("transport.hashSize", spec.getHash().getBlockSize(), policyConfig.getTransportMinHashBlockSize()); //$NON-NLS-1$
        }

        return null;
    }

}
