/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.x500.X500Name;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.crypto.x509.CertExtension;


/**
 * @author mbechler
 *
 */
public class GenerateSignedCertificateConfigurator extends
        AbstractConfigurator<StatusOnlyResult, GenerateSignedCertificate, GenerateSignedCertificateConfigurator> {

    /**
     * @param unit
     */
    protected GenerateSignedCertificateConfigurator ( GenerateSignedCertificate unit ) {
        super(unit);
    }


    /**
     * @param ksName
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator keystore ( String ksName ) {
        this.getExecutionUnit().setKeystoreName(ksName);
        return this.self();
    }


    /**
     * 
     * @param signKsName
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator signingKeystore ( String signKsName ) {
        this.getExecutionUnit().setSigningKeystore(signKsName);
        return this.self();
    }


    /**
     * 
     * @param signKeyAlias
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator signingKeyAlias ( String signKeyAlias ) {
        this.getExecutionUnit().setSigningAlias(signKeyAlias);
        return this.self();
    }


    /**
     * @param alias
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator alias ( String alias ) {
        this.getExecutionUnit().setAlias(alias);
        return this.self();
    }


    /**
     * @param subject
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator subject ( X500Name subject ) {
        this.getExecutionUnit().setSubject(subject);
        return this.self();
    }


    /**
     * @param exts
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator extensions ( Set<CertExtension> exts ) {
        this.getExecutionUnit().setExtensions(exts);
        return this.self();
    }


    /**
     * 
     * @param validFrom
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator validFrom ( DateTime validFrom ) {
        this.getExecutionUnit().setValidFrom(validFrom);
        return this.self();
    }


    /**
     * 
     * @param validTo
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator validTo ( DateTime validTo ) {
        this.getExecutionUnit().setValidTo(validTo);
        return this.self();
    }


    /**
     * 
     * @param extraChain
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator extraChain ( X509Certificate[] extraChain ) {
        this.getExecutionUnit().setExtraChain(extraChain);
        return this.self();
    }


    /**
     * 
     * @param extraChain
     * @return this configurator
     */
    public GenerateSignedCertificateConfigurator extraChain ( List<X509Certificate> extraChain ) {
        return this.extraChain(extraChain.toArray(new X509Certificate[] {}));
    }

}
