/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 24, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigTestParams;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigTestService;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.test.ConfigTestBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.validation.domain.ValidFQDN;


/**
 * @author mbechler
 *
 */
@Named ( "hc_resolver_testBean" )
@ViewScoped
public class ResolverConfigTestBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8079146099548035795L;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private ConfigTestBean configTest;

    private String hostname;


    /**
     * @return the hostname
     */
    @ValidFQDN
    public String getHostname () {
        return this.hostname;
    }


    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname ( String hostname ) {
        this.hostname = hostname;
    }


    public String test ( ConfigContext<ConfigurationObject, ?> ctx ) {
        try {
            ConfigurationObject curCfg = ctx.getCurrent();
            ResolverConfigTestParams tp = new ResolverConfigTestParams();
            tp.setHostname(getHostname());
            this.configTest.setResults(this.ssp.getService(ConfigTestService.class)
                    .test(ctx.getAnchor(), curCfg, this.configTest.getObjectType(), this.configTest.getObjectPath(), tp));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

}
