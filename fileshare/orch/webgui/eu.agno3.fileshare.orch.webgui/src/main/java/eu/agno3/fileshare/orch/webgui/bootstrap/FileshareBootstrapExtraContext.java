/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 2, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.bootstrap;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "fs_bootstrapExtraContext" )
public class FileshareBootstrapExtraContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6011697441327460157L;

    private boolean createUser = true;
    private boolean createUserAdmin = true;
    private String createUserName = "fsadmin"; //$NON-NLS-1$
    private String createUserPassword;

    @Inject
    private CoreServiceProvider csp;


    /**
     * 
     * @return the password policy checker
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.csp.getPasswordPolicy();
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
