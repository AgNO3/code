/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "orch_userCreateContext" )
public class UserCreateContext implements Serializable {

    /**
     * 
     */
    private static final String ADMIN_CREATED_USER = "ADMIN_CREATED_USER"; //$NON-NLS-1$
    private static final String DEFAULT_USER = "DEFAULT_USER"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -7807597353902394431L;

    private List<String> roles;
    private String userName;
    private String password;
    private boolean forcePasswordChange;
    private boolean disabled;


    @PostConstruct
    protected void init () {
        this.roles = new ArrayList<>(Arrays.asList(ADMIN_CREATED_USER, DEFAULT_USER));
    }


    /**
     * 
     */
    public void reset () {
        this.roles = new ArrayList<>(Arrays.asList(ADMIN_CREATED_USER, DEFAULT_USER));

    }


    /**
     * @return the userName
     */
    public String getUserName () {
        return this.userName;
    }


    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName ( String userName ) {
        this.userName = userName;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.password = password;
    }


    /**
     * @return the forcePasswordChange
     */
    public boolean getForcePasswordChange () {
        return this.forcePasswordChange;
    }


    /**
     * @param forcePasswordChange
     *            the forcePasswordChange to set
     */
    public void setForcePasswordChange ( boolean forcePasswordChange ) {
        this.forcePasswordChange = forcePasswordChange;
    }


    /**
     * @return the disabled
     */
    public boolean getDisabled () {
        return this.disabled;
    }


    /**
     * @param disabled
     *            the disabled to set
     */
    public void setDisabled ( boolean disabled ) {
        this.disabled = disabled;
    }


    /**
     * @return the permissions
     */
    public List<String> getRoles () {
        return this.roles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles ( List<String> roles ) {
        this.roles = roles;
    }

}
