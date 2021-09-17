/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 24, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.UserPasswordAuthTestParams;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigTestService;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.test.ConfigTestBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "auth_authenticator_ad_testBean" )
@ViewScoped
public class ADAuthConfigTestBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8079146099548035795L;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private ConfigTestBean configTest;

    private String username;

    private String password;


    /**
     * @return the username
     */
    public String getUsername () {
        return this.username;
    }


    /**
     * @param username
     *            the username to set
     */
    public void setUsername ( String username ) {
        this.username = username;
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


    public String test ( ConfigContext<ConfigurationObject, ?> ctx ) {
        try {
            ConfigurationObject curCfg = ctx.getCurrent();
            UserPasswordAuthTestParams tp = new UserPasswordAuthTestParams();
            tp.setUsername(this.getUsername());
            tp.setPassword(this.getPassword());
            this.configTest.setResults(
                this.ssp.getService(ConfigTestService.class)
                        .test(ctx.getAnchor(), curCfg, this.configTest.getObjectType(), this.configTest.getObjectPath(), tp));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

}
