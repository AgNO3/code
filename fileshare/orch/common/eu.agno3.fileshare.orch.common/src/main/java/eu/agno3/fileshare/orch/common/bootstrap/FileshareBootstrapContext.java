/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 28, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.bootstrap;


import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;


/**
 * @author mbechler
 *
 */
public class FileshareBootstrapContext extends BootstrapContext {

    private ServiceStructuralObject fileshareService;
    private FileshareConfiguration fileshareConfig;

    private boolean createUser;
    private boolean createUserAdmin;
    private String createUserName;
    private String createUserPassword;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.bootstrap.BootstrapContext#getType()
     */
    @Override
    public String getType () {
        return "fileshare"; //$NON-NLS-1$
    }


    /**
     * @return fileshare service to configure
     */
    public ServiceStructuralObject getFileshareService () {
        return this.fileshareService;
    }


    /**
     * @param fileshareService
     *            the fileshareService to set
     */
    public void setFileshareService ( ServiceStructuralObject fileshareService ) {
        this.fileshareService = fileshareService;
    }


    /**
     * @return the fileshareConfig
     */
    public FileshareConfiguration getFileshareConfig () {
        return this.fileshareConfig;
    }


    /**
     * @param fileshareConfig
     *            the fileshareConfig to set
     */
    public void setFileshareConfig ( FileshareConfiguration fileshareConfig ) {
        this.fileshareConfig = fileshareConfig;
    }


    /**
     * @return the createUser
     */
    public boolean getCreateUser () {
        return this.createUser;
    }


    /**
     * @param createUser
     *            the createUser to set
     */
    public void setCreateUser ( boolean createUser ) {
        this.createUser = createUser;
    }


    /**
     * @return the createUserAdmin
     */
    public boolean getCreateUserAdmin () {
        return this.createUserAdmin;
    }


    /**
     * @param createUserAdmin
     *            the createUserAdmin to set
     */
    public void setCreateUserAdmin ( boolean createUserAdmin ) {
        this.createUserAdmin = createUserAdmin;
    }


    /**
     * @return the createUserName
     */
    public String getCreateUserName () {
        return this.createUserName;
    }


    /**
     * @param createUserName
     *            the createUserName to set
     */
    public void setCreateUserName ( String createUserName ) {
        this.createUserName = createUserName;
    }


    /**
     * @return the createUserPassword
     */
    public String getCreateUserPassword () {
        return this.createUserPassword;
    }


    /**
     * @param createUserPassword
     *            the createUserPassword to set
     */
    public void setCreateUserPassword ( String createUserPassword ) {
        this.createUserPassword = createUserPassword;
    }
}
