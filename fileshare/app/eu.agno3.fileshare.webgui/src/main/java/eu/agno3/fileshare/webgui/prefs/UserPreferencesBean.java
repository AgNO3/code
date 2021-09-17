/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.prefs;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean;


/**
 * @author mbechler
 *
 */
@SessionScoped
@Named ( "userPreferencesBean" )
public class UserPreferencesBean extends AbstractPreferencesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3923091466472602520L;

    private static final String TREE_FILE_VIEW = "treeFileView"; //$NON-NLS-1$
    private static final String DISABLE_EXPIRATION_NOTIFICATION = "disableExpirationNotification"; //$NON-NLS-1$

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private CurrentUserBean currentUser;

    private Boolean treeFileView;
    private Boolean disableExpirationNotification;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#isAuthenticated()
     */
    @Override
    protected boolean isAuthenticated () {
        return this.currentUser.isAuthenticated();
    }


    /**
     * @return whether the user perfers a single level file view
     */
    public boolean getTreeFileView () {
        return this.treeFileView == null ? false : this.treeFileView;
    }


    /**
     * @param b
     */
    public void setTreeFileView ( boolean b ) {
        this.treeFileView = b;
    }


    /**
     * @param disableExpirationNotification
     *            the disableExpirationNotification to set
     */
    public void setDisableExpirationNotification ( Boolean disableExpirationNotification ) {
        this.disableExpirationNotification = disableExpirationNotification;
    }


    /**
     * @return the disableExpirationNotification
     */
    public Boolean getDisableExpirationNotification () {
        return this.disableExpirationNotification;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#loadPreferencesInternal()
     */
    @Override
    protected Map<String, String> loadPreferencesInternal () {
        try {
            return this.fsp.getPreferenceService().loadPreferences();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return new HashMap<>();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.prefs.AbstractPreferencesBean#savePreferences(java.util.Map)
     */
    @Override
    protected Map<String, String> savePreferences ( Map<String, String> hashMap ) {
        try {
            return this.fsp.getPreferenceService().savePreferences(hashMap);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
    }
    }


    /**
     * @param vals
     * @return
     */
    @Override
    protected Map<String, String> toMap () {
        Map<String, String> vals = super.toMap();

        if ( this.treeFileView != null ) {
            vals.put(TREE_FILE_VIEW, this.treeFileView.toString());
        }

        if ( this.disableExpirationNotification != null ) {
            vals.put(DISABLE_EXPIRATION_NOTIFICATION, this.disableExpirationNotification.toString());
        }

        return vals;
    }


    /**
     * @param vals
     */
    @Override
    protected void fromMap ( Map<String, String> vals ) {
        super.fromMap(vals);
        if ( vals.containsKey(TREE_FILE_VIEW) ) {
            this.treeFileView = Boolean.parseBoolean(vals.get(TREE_FILE_VIEW));
        }
        else {
            this.treeFileView = false;
        }

        if ( vals.containsKey(DISABLE_EXPIRATION_NOTIFICATION) ) {
            this.disableExpirationNotification = Boolean.parseBoolean(vals.get(DISABLE_EXPIRATION_NOTIFICATION));
        }
        else {
            this.disableExpirationNotification = false;
        }
    }

}
