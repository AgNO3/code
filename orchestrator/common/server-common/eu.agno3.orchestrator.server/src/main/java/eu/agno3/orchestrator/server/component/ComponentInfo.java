/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.component;


import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class ComponentInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1538269936272126630L;

    private UUID componentId;
    private ComponentState state;
    private String lastKnownAddress;
    private ProvisioningState provisionState;

    private String lastKnownHostName;
    private X509Certificate certificate;


    /**
     * 
     */
    public ComponentInfo () {
        super();
    }


    /**
     * @return the agentId
     */
    public UUID getComponentId () {
        return this.componentId;
    }


    /**
     * @param agentId
     *            the agentId to set
     */
    public void setComponentId ( UUID agentId ) {
        this.componentId = agentId;
    }


    /**
     * @return the state
     */
    public ComponentState getState () {
        return this.state;
    }


    /**
     * @param state
     *            the state to set
     */
    public void setState ( ComponentState state ) {
        this.state = state;
    }


    /**
     * @return the lastKnownAddress
     */
    public String getLastKnownAddress () {
        return this.lastKnownAddress;
    }


    /**
     * @param lastKnownAddress
     *            the lastKnownAddress to set
     */
    public void setLastKnownAddress ( String lastKnownAddress ) {
        this.lastKnownAddress = lastKnownAddress;
    }


    /**
     * @return the lastKnownHostName
     */
    public String getLastKnownHostName () {
        return this.lastKnownHostName;
    }


    /**
     * @param lastKnownHostName
     *            the lastKnownHostName to set
     */
    public void setLastKnownHostName ( String lastKnownHostName ) {
        this.lastKnownHostName = lastKnownHostName;
    }


    /**
     * @return the provisionState
     */
    public ProvisioningState getProvisionState () {
        return this.provisionState;
    }


    /**
     * @param provisionState
     */
    public void setProvisionState ( ProvisioningState provisionState ) {
        this.provisionState = provisionState;
    }


    /**
     * @return the certificate
     */
    public X509Certificate getCertificate () {
        return this.certificate;
    }


    /**
     * @param certificate
     */
    public void setCertificate ( X509Certificate certificate ) {
        this.certificate = certificate;
    }
}