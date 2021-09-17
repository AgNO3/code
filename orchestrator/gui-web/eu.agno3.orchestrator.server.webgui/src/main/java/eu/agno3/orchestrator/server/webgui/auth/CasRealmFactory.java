/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.security.cas.client.AbstractCasRealm;
import eu.agno3.runtime.security.cas.client.CasAuthConfiguration;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class CasRealmFactory {

    @Inject
    @OsgiService ( dynamic = true, timeout = 500, filter = "(subsystem=webgui/casClient)" )
    private TLSContext tlsContext;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private GuiWsClientFactory wsClientFactory;


    public AbstractCasRealm createRealm ( CasAuthConfiguration authConfig ) {
        return new WebServiceAuthRealm(this.tlsContext, authConfig, this.wsClientFactory);
    }
}
