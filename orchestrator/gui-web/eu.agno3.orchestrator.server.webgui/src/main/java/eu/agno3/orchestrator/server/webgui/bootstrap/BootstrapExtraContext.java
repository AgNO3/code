/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.bootstrap;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "bootstrapExtraContext" )
public class BootstrapExtraContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3101833584502534433L;
    private String adminPassword;
    private boolean completed;
    private boolean failed;
    private String step = "changeAdminPassword"; //$NON-NLS-1$

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private BootstrapContextProvider bcp;
    private String guiUri;


    /**
     * @return the adminPassword
     */
    public String getAdminPassword () {
        return this.adminPassword;
    }


    /**
     * @param adminPassword
     *            the adminPassword to set
     */
    public void setAdminPassword ( String adminPassword ) {
        this.adminPassword = adminPassword;
    }


    public boolean getForcePasswordChange () {
        BootstrapContext context = this.bcp.getContext();
        if ( context != null ) {
            return context.getRequirePasswordChange();
        }
        return false;
    }


    public int getPasswordMinimumEntropy () {
        BootstrapContext context = this.bcp.getContext();
        if ( context != null ) {
            return context.getMinimumPasswordEntropy();
        }
        return 0;
    }


    /**
     * 
     * @return the password policy checker
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.csp.getPasswordPolicy();
    }


    /**
     * @return whether the bootstrap is already completed
     */
    public boolean isCompleted () {
        return this.completed;
    }


    /**
     * @param completed
     *            the completed to set
     */
    public void setCompleted ( boolean completed ) {
        this.completed = completed;
    }


    /**
     * @return the failed
     */
    public boolean isFailed () {
        return this.failed;
    }


    /**
     * @param failed
     *            the failed to set
     */
    public void setFailed ( boolean failed ) {
        this.failed = failed;
    }


    /**
     * @return the step
     */
    public String getStep () {
        return this.step;
    }


    /**
     * @param step
     *            the step to set
     */
    public void setStep ( String step ) {
        this.step = step;
    }


    /**
     * @return whether to continue
     */
    public boolean validateExtra () {
        return true;
    }


    /**
     * @param guiUri
     */
    public void setGuiUri ( String guiUri ) {
        this.guiUri = guiUri;
    }


    /**
     * @return the guiUri
     */
    public String getGuiUri () {
        return this.guiUri;
    }
}
