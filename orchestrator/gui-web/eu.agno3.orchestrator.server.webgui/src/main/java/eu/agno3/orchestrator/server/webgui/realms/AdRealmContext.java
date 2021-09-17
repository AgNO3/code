/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "adRealmContext" )
public class AdRealmContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6908873108481639345L;
    private String domain;
    private String adminUsername;
    private String adminPassword;

    private boolean joinWithResetPassword;

    @Inject
    private InstanceRealmManager irm;
    private String initialMachinePassword;


    /**
     * @return the domain
     */
    public String getDomain () {
        return this.domain;
    }


    /**
     * @param domain
     *            the domain to set
     */
    public void setDomain ( String domain ) {
        this.domain = domain;
    }


    /**
     * @return the adminUsername
     */
    public String getAdminUsername () {
        return this.adminUsername;
    }


    /**
     * @param adminUsername
     *            the adminUsername to set
     */
    public void setAdminUsername ( String adminUsername ) {
        this.adminUsername = adminUsername;
    }


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


    /**
     * @return the joinWithResetPassword
     */
    public boolean getJoinWithResetPassword () {
        return this.joinWithResetPassword;
    }


    /**
     * @param joinWithResetPassword
     *            the joinWithResetPassword to set
     */
    public void setJoinWithResetPassword ( boolean joinWithResetPassword ) {
        this.joinWithResetPassword = joinWithResetPassword;
    }


    /**
     * @return initial machine password
     */
    public String getInitialMachinePassword () {
        return this.initialMachinePassword;
    }


    /**
     * @param initialMachinePassword
     *            the initialMachinePassword to set
     */
    public void setInitialMachinePassword ( String initialMachinePassword ) {
        this.initialMachinePassword = initialMachinePassword;
    }


    public String joinDomain () {
        try {
            if ( this.joinWithResetPassword ) {
                this.irm.joinDomainWithMachinePassword(getDomain(), getInitialMachinePassword());
            }
            else {
                this.irm.joinDomain(getDomain(), getAdminUsername(), getAdminPassword());
            }
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String leaveDomain () {
        try {
            this.irm.leaveDomain(getDomain(), getAdminUsername(), getAdminPassword());
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String rekey () {
        try {
            this.irm.rekeyDomain(getDomain());
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }
}
