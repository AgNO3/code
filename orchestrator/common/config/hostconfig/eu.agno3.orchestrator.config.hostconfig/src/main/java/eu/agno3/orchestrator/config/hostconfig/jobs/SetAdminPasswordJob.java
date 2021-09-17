/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.jobs;


import eu.agno3.orchestrator.config.model.jobs.SystemJob;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public class SetAdminPasswordJob extends SystemJob {

    private WrappedCredentials oldCredentials;
    private WrappedCredentials newCredentials;
    private String adminPassword;


    /**
     * @return the newCredentials
     */
    public WrappedCredentials getNewCredentials () {
        return this.newCredentials;
    }


    /**
     * @param newCredentials
     *            the newCredentials to set
     */
    public void setNewCredentials ( WrappedCredentials newCredentials ) {
        this.newCredentials = newCredentials;
    }


    /**
     * @return the oldCredentials
     */
    public WrappedCredentials getOldCredentials () {
        return this.oldCredentials;
    }


    /**
     * @param oldCredentials
     *            the oldCredentials to set
     */
    public void setOldCredentials ( WrappedCredentials oldCredentials ) {
        this.oldCredentials = oldCredentials;
    }


    /**
     * @param adminPassword
     */
    public void setAdminPassword ( String adminPassword ) {
        this.adminPassword = adminPassword;
    }


    /**
     * @return the adminPassword
     */
    public String getAdminPassword () {
        return this.adminPassword;
    }

}
