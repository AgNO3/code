/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.server.data;


import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.component.ProvisioningState;
import eu.agno3.orchestrator.types.crypto.X509CertificateJPAConverter;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "orchestrator" )
@Entity
@Table ( name = "agent_state_cache2" )
public class AgentStateCacheEntry {

    private UUID agentId;
    private long version;

    private ProvisioningState provisionState;
    private ComponentState cachedState;

    private String lastKnownAddr;
    private DateTime lastStateChange;

    private String imageType;
    private byte[] publicKeyFingerprint;
    private String lastKnownHostName;
    private X509Certificate certificate;


    /**
     * 
     * @return the object id
     */
    @Id
    @Column ( length = 16 )
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setAgentId ( UUID id ) {
        this.agentId = id;
    }


    /**
     * 
     * @return the version
     */
    @Version
    public long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( long version ) {
        this.version = version;
    }


    /**
     * @return the cachedState
     */
    @OptimisticLock ( excluded = true )
    @Enumerated ( EnumType.STRING )
    public ComponentState getCachedState () {
        return this.cachedState;
    }


    /**
     * @param cachedState
     *            the cachedState to set
     */
    public void setCachedState ( ComponentState cachedState ) {
        this.cachedState = cachedState;
    }


    /**
     * @return the provisionState
     */
    @Enumerated ( EnumType.STRING )
    public ProvisioningState getProvisionState () {
        return this.provisionState;
    }


    /**
     * @param provisionState
     *            the provisionState to set
     */
    public void setProvisionState ( ProvisioningState provisionState ) {
        this.provisionState = provisionState;
    }


    /**
     * @return the lastSeen
     */
    @OptimisticLock ( excluded = true )
    public DateTime getLastStateChange () {
        return this.lastStateChange;
    }


    /**
     * @param lastSeen
     *            the lastSeen to set
     */
    public void setLastStateChange ( DateTime lastSeen ) {
        this.lastStateChange = lastSeen;
    }


    /**
     * @return the lastKnownAddr
     */
    public String getLastKnownAddr () {
        return this.lastKnownAddr;
    }


    /**
     * @param lastKnownAddr
     *            the lastKnownAddr to set
     */
    public void setLastKnownAddr ( String lastKnownAddr ) {
        this.lastKnownAddr = lastKnownAddr;
    }


    /**
     * @return the lastKnownHostName
     */
    public String getLastKnownHostName () {
        return this.lastKnownHostName;
    }


    /**
     * @param hostName
     */
    public void setLastKnownHostName ( String hostName ) {
        this.lastKnownHostName = hostName;
    }


    /**
     * @return the publicKeyFingerprint
     */
    @Column ( length = 32 )
    public byte[] getPublicKeyFingerprint () {
        return this.publicKeyFingerprint;
    }


    /**
     * @param publicKeyFingerprint
     *            the publicKeyFingerprint to set
     */
    public void setPublicKeyFingerprint ( byte[] publicKeyFingerprint ) {
        this.publicKeyFingerprint = publicKeyFingerprint;
    }


    /**
     * @return the imageType
     */
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }


    /**
     * @return component certificate
     */
    @Lob
    @Convert ( converter = X509CertificateJPAConverter.class )
    public X509Certificate getCertificate () {
        return this.certificate;
    }


    /**
     * @param certificate
     *            the certificate to set
     */
    public void setCertificate ( X509Certificate certificate ) {
        this.certificate = certificate;
    }

}
