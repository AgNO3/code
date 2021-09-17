/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.12.2014 by mbechler
 */
package eu.agno3.orchestrator.crypto.jobs;


import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.jobs.ConfigurationJobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;


/**
 * @author mbechler
 *
 */
public class RegenerateInternalCertJob extends JobImpl {

    private String keyStore;
    private String keyAlias;
    private X500Name subject;
    private Set<HostOrAddress> sanAddresses;
    private boolean includeCA;
    private Duration lifetime;
    private int keyUsage;
    private Set<ASN1ObjectIdentifier> extendedKeyUsages;


    /**
     * 
     */
    public RegenerateInternalCertJob () {
        super(new ConfigurationJobGroup());
    }


    /**
     * @return the keyStore
     */
    public String getKeyStore () {
        return this.keyStore;
    }


    /**
     * @param keyStore
     *            the keyStore to set
     */
    public void setKeyStore ( String keyStore ) {
        this.keyStore = keyStore;
    }


    /**
     * @return the keyAlias
     */
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * @param keyAlias
     *            the keyAlias to set
     */
    public void setKeyAlias ( String keyAlias ) {
        this.keyAlias = keyAlias;
    }


    /**
     * @return the subject
     */
    public X500Name getSubject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject ( X500Name subject ) {
        this.subject = subject;
    }


    /**
     * @return the subject alt names
     */
    public Set<HostOrAddress> getSanAdresses () {
        return this.sanAddresses;
    }


    /**
     * @param sanAddresses
     *            the sanAddresses to set
     */
    public void setSanAddresses ( Set<HostOrAddress> sanAddresses ) {
        this.sanAddresses = sanAddresses;
    }


    /**
     * @return the lifetime
     */
    public Duration getLifetime () {
        return this.lifetime;
    }


    /**
     * @param lifetime
     *            the lifetime to set
     */
    public void setLifetime ( Duration lifetime ) {
        this.lifetime = lifetime;
    }


    /**
     * @return the requested exteded key usages
     */
    public Set<ASN1ObjectIdentifier> getExtendedKeyUsage () {
        return this.extendedKeyUsages;
    }


    /**
     * @param extendedKeyUsages
     *            the extendedKeyUsages to set
     */
    public void setExtendedKeyUsages ( Set<ASN1ObjectIdentifier> extendedKeyUsages ) {
        this.extendedKeyUsages = extendedKeyUsages;
    }


    /**
     * @return the requested key usage
     */
    public int getKeyUsage () {
        return this.keyUsage;
    }


    /**
     * @param keyUsage
     *            the keyUsage to set
     */
    public void setKeyUsage ( int keyUsage ) {
        this.keyUsage = keyUsage;
    }


    /**
     * @param includeCA
     *            the includeCA to set
     */
    public void setIncludeCA ( boolean includeCA ) {
        this.includeCA = includeCA;
    }


    /**
     * @return the includeCA
     */
    public boolean getIncludeCA () {
        return this.includeCA;
    }

}
