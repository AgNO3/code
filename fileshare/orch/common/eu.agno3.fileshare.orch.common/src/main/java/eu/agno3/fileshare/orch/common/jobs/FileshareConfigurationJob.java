/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.jobs;


import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;


/**
 * @author mbechler
 * 
 */
public class FileshareConfigurationJob extends ConfigurationJob {

    private FileshareConfiguration fileshareConfig;

    private boolean createInitialUser;
    private Set<String> createInitialUserRoles;
    private String createInitialUserName;
    private String createInitialUserPasswordHash;


    /**
     * @param fileshareConfig
     *            the fileshareConfig to set
     */
    public void setFileshareConfig ( @NonNull FileshareConfiguration fileshareConfig ) {
        this.fileshareConfig = fileshareConfig;
    }


    /**
     * @return the hostConfig
     */
    public FileshareConfiguration getFileshareConfig () {
        return this.fileshareConfig;
    }


    /**
     * @return the createInitialUser
     */
    public boolean getCreateInitialUser () {
        return this.createInitialUser;
    }


    /**
     * @param createInitialUser
     *            the createInitialUser to set
     */
    public void setCreateInitialUser ( boolean createInitialUser ) {
        this.createInitialUser = createInitialUser;
    }


    /**
     * @return the createInitialUserName
     */
    public String getCreateInitialUserName () {
        return this.createInitialUserName;
    }


    /**
     * @param createInitialUserName
     *            the createInitialUserName to set
     */
    public void setCreateInitialUserName ( String createInitialUserName ) {
        this.createInitialUserName = createInitialUserName;
    }


    /**
     * @return the createInitialUserPassword
     */
    public String getCreateInitialUserPasswordHash () {
        return this.createInitialUserPasswordHash;
    }


    /**
     * @param createInitialUserPasswordHash
     *            the createInitialUserPassword to set
     */
    public void setCreateInitialUserPasswordHash ( String createInitialUserPasswordHash ) {
        this.createInitialUserPasswordHash = createInitialUserPasswordHash;
    }


    /**
     * @return the createInitialUserRoles
     */
    public Set<String> getCreateInitialUserRoles () {
        return this.createInitialUserRoles;
    }


    /**
     * @param createInitialUserRoles
     *            the createInitialUserRoles to set
     */
    public void setCreateInitialUserRoles ( Set<String> createInitialUserRoles ) {
        this.createInitialUserRoles = createInitialUserRoles;
    }

}
