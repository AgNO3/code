/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 11, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.service.InstanceManageService;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.crypto.InstanceCredentialWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.jobs.JobDetailContextBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.security.credentials.UsernamePasswordCredential;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "changeShellPasswordBean" )
public class ChangeShellPasswordBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4607460320212011714L;

    private String oldPassword;
    private String newPassword;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private JobDetailContextBean jobContext;

    @Inject
    private InstanceCredentialWrapper credWrapper;


    public PasswordPolicyChecker getPasswordPolicy () {
        return this.csp.getPasswordPolicy();
    }


    public String getOldPassword () {
        return this.oldPassword;
    }


    /**
     * @param oldPassword
     *            the oldPassword to set
     */
    public void setOldPassword ( String oldPassword ) {
        this.oldPassword = oldPassword;
    }


    /**
     * @return the newPassword
     */
    public String getNewPassword () {
        return this.newPassword;
    }


    /**
     * @param newPassword
     *            the newPassword to set
     */
    public void setNewPassword ( String newPassword ) {
        this.newPassword = newPassword;
    }


    public String changePassword () {
        try {
            InstanceStructuralObject inst = this.structureContext.getSelectedInstance();

            WrappedCredentials nc = this.credWrapper.wrap(inst, new UsernamePasswordCredential(null, getNewPassword()));
            WrappedCredentials oc = getOldPassword() != null ? this.credWrapper.wrap(inst, new UsernamePasswordCredential(null, getOldPassword()))
                    : null;

            @NonNull
            JobInfo setAdministratorPassword = this.ssp.getService(InstanceManageService.class).setAdministratorPassword(inst, oc, nc);
            this.jobContext.clear();
            this.jobContext.setJobId(setAdministratorPassword.getJobId());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }
}
