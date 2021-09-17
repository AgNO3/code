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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigTestParams;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigTestService;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.test.ConfigTestBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.types.net.validation.ValidHostOrAddress;


/**
 * @author mbechler
 *
 */
@Named ( "hc_network_testBean" )
@ViewScoped
public class NetworkConfigTestBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8079146099548035795L;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private ConfigTestBean configTest;

    private String target;

    private String port;

    private boolean runPing = true;
    private boolean runTraceroute;


    /**
     * @return the target
     */
    @ValidHostOrAddress
    public String getTarget () {
        return this.target;
    }


    /**
     * @param target
     *            the target to set
     */
    public void setTarget ( String target ) {
        this.target = target;
    }


    /**
     * @return the port
     */
    @Range ( min = 1, max = 65535 )
    public String getPort () {
        return this.port;
    }


    /**
     * @param port
     *            the port to set
     */
    public void setPort ( String port ) {
        this.port = port;
    }


    /**
     * @return the runPing
     */
    public boolean getRunPing () {
        return this.runPing;
    }


    /**
     * @param runPing
     *            the runPing to set
     */
    public void setRunPing ( boolean runPing ) {
        this.runPing = runPing;
    }


    /**
     * @return the runTraceroute
     */
    public boolean getRunTraceroute () {
        return this.runTraceroute;
    }


    /**
     * @param runTraceroute
     *            the runTraceroute to set
     */
    public void setRunTraceroute ( boolean runTraceroute ) {
        this.runTraceroute = runTraceroute;
    }


    public String test ( ConfigContext<ConfigurationObject, ?> ctx ) {
        try {
            ConfigurationObject curCfg = ctx.getCurrent();
            NetworkConfigTestParams tp = new NetworkConfigTestParams();
            tp.setTarget(getTarget());
            if ( !StringUtils.isBlank(this.port) ) {
                tp.setPort(Integer.valueOf(this.port));
            }
            tp.setRunPing(this.runPing);
            tp.setRunTraceroute(this.runTraceroute);
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
