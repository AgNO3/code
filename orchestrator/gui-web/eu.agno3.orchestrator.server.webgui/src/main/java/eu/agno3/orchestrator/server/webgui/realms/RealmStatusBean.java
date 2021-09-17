/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 31, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.service.RealmManagementService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "realmStatusBean" )
public class RealmStatusBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4011826822864149254L;

    @Inject
    private ServerServiceProvider ssp;

    private Map<String, RealmInfo> cached = new HashMap<>();


    public RealmInfo getStatus ( InstanceStructuralObject anchor, String realm ) {
        try {
            String urlm = realm.toUpperCase(Locale.ROOT);
            RealmInfo c = this.cached.get(urlm);
            if ( c != null ) {
                return c;
            }

            RealmInfo ri = this.ssp.getService(RealmManagementService.class).getRealm(anchor, realm);
            this.cached.put(urlm, ri);
            return ri;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public void refresh () {
        this.cached.clear();
    }


    public void refresh ( AjaxBehaviorEvent ev ) {
        refresh();
    }
}
