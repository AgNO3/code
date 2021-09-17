/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.prefs;


import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;

import eu.agno3.orchestrator.server.session.service.SessionService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.menu.MenuState;
import eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean;


/**
 * @author mbechler
 *
 */
@Named ( "userPreferences" )
@SessionScoped
public class UserPreferencesBean extends AbstractPreferencesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 423127571570440132L;

    private static final String DEFAULT_DETAIL_LEVEL = "defaultDetailLevel"; //$NON-NLS-1$
    private static final String ENABLE_EXPERIMENTAL = "enableExperimentalFeatures"; //$NON-NLS-1$
    private static final String ENABLE_MULTI_HOST = "enableMultiHostManagement"; //$NON-NLS-1$
    private static final String ENABLE_DEVELOPER_MODE = "enableDeveloperMode"; //$NON-NLS-1$

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private MenuState msb;

    private Integer defaultDetailLevel;
    private Boolean enableExperimentalFeatures;
    private Boolean enableMultiHostManagement;
    private Boolean enableDeveloperMode;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#isAuthenticated()
     */
    @Override
    protected boolean isAuthenticated () {
        return SecurityUtils.getSubject().isAuthenticated();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#savePreferences(java.util.Map)
     */
    @Override
    protected Map<String, String> savePreferences ( Map<String, String> hashMap ) {
        try {
            return this.ssp.getService(SessionService.class).savePreferences(hashMap);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#loadPreferencesInternal()
     */
    @Override
    protected Map<String, String> loadPreferencesInternal () {
        try {
            return this.ssp.getService(SessionService.class).loadPreferences();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#preferencesChanged()
     */
    @Override
    protected void preferencesChanged () {
        super.preferencesChanged();
        this.msb.reload();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#toMap()
     */
    @Override
    protected Map<String, String> toMap () {
        Map<String, String> prefs = super.toMap();

        if ( this.defaultDetailLevel != null ) {
            prefs.put(DEFAULT_DETAIL_LEVEL, this.defaultDetailLevel.toString());
        }

        if ( this.enableExperimentalFeatures != null ) {
            prefs.put(ENABLE_EXPERIMENTAL, this.enableExperimentalFeatures.toString());
        }

        if ( this.enableMultiHostManagement != null ) {
            prefs.put(ENABLE_MULTI_HOST, this.enableMultiHostManagement.toString());
        }

        if ( this.enableDeveloperMode != null ) {
            prefs.put(ENABLE_DEVELOPER_MODE, this.enableDeveloperMode.toString());
        }

        return prefs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#fromMap(java.util.Map)
     */
    @Override
    protected void fromMap ( Map<String, String> vals ) {
        super.fromMap(vals);

        if ( vals.containsKey(DEFAULT_DETAIL_LEVEL) ) {
            this.defaultDetailLevel = Integer.parseInt(vals.get(DEFAULT_DETAIL_LEVEL));
        }
        else {
            this.defaultDetailLevel = null;
        }

        if ( vals.containsKey(ENABLE_EXPERIMENTAL) ) {
            this.enableExperimentalFeatures = Boolean.parseBoolean(vals.get(ENABLE_EXPERIMENTAL));
        }
        else {
            this.enableExperimentalFeatures = null;
        }

        if ( vals.containsKey(ENABLE_MULTI_HOST) ) {
            this.enableMultiHostManagement = Boolean.parseBoolean(vals.get(ENABLE_MULTI_HOST));
        }
        else {
            this.enableMultiHostManagement = null;
        }

        if ( vals.containsKey(ENABLE_DEVELOPER_MODE) ) {
            this.enableDeveloperMode = Boolean.parseBoolean(vals.get(ENABLE_DEVELOPER_MODE));
        }
        else {
            this.enableDeveloperMode = null;
        }

    }


    /**
     * 
     * @return the users default detail level
     */
    public int getDefaultDetailLevel () {
        if ( this.defaultDetailLevel == null ) {
            return 1;
        }
        return this.defaultDetailLevel;
    }


    /**
     * @param defaultDetailLevel
     *            the defaultDetailLevel to set
     */
    public void setDefaultDetailLevel ( int defaultDetailLevel ) {
        this.defaultDetailLevel = defaultDetailLevel;
    }


    /**
     * @return whether to enable experimental features
     */
    public boolean getEnableExperimentalFeatures () {
        if ( this.enableExperimentalFeatures == null ) {
            return false;
        }
        return this.enableExperimentalFeatures;
    }


    /**
     * @param enableExperimentalFeatures
     *            the enableExperimentalFeatures to set
     */
    public void setEnableExperimentalFeatures ( boolean enableExperimentalFeatures ) {
        this.enableExperimentalFeatures = enableExperimentalFeatures;
    }


    /**
     * @return whether to enable multi host mangement features
     */
    public boolean getEnableMultiHostManagement () {
        if ( this.enableMultiHostManagement == null ) {
            return false;
        }
        return this.enableMultiHostManagement;
    }


    /**
     * @param enableMultiHostManagement
     *            the enableMultiHostManagement to set
     */
    public void setEnableMultiHostManagement ( boolean enableMultiHostManagement ) {
        this.enableMultiHostManagement = enableMultiHostManagement;
    }


    /**
     * @return whether to enable deveoper mode
     */
    public boolean getEnableDeveloperMode () {
        if ( this.enableDeveloperMode == null ) {
            return false;
        }
        return this.enableDeveloperMode;
    }


    /**
     * @param enableDeveloperMode
     *            the enableDeveloperMode to set
     */
    public void setEnableDeveloperMode ( boolean enableDeveloperMode ) {
        this.enableDeveloperMode = enableDeveloperMode;
    }

}
