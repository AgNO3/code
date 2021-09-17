/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp;


import java.util.HashMap;
import java.util.Map;

import eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean;


/**
 * @author mbechler
 *
 */
public class UserPreferencesBean extends AbstractPreferencesBean {

    /**
     * 
     */
    private static final long serialVersionUID = 525543433543937515L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#isAuthenticated()
     */
    @Override
    protected boolean isAuthenticated () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#savePreferences(java.util.Map)
     */
    @Override
    protected Map<String, String> savePreferences ( Map<String, String> hashMap ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#loadPreferencesInternal()
     */
    @Override
    protected Map<String, String> loadPreferencesInternal () {
        return new HashMap<>();
    }

}
