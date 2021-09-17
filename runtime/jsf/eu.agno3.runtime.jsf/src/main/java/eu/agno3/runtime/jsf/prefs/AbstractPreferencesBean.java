/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.runtime.jsf.prefs;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;


/**
 * @author mbechler
 *
 */
public abstract class AbstractPreferencesBean implements Serializable, UserPreferences {

    /**
     * 
     */
    private static final long serialVersionUID = 1276702621686845767L;

    private static final Logger log = Logger.getLogger(AbstractPreferencesBean.class);

    private static final String OVERRIDE_LOCALE = "overrideLocale"; //$NON-NLS-1$
    private static final String OVERRIDE_DATE_LOCALE = "overrideDateLocale"; //$NON-NLS-1$
    private static final String OVERRIDE_TIMEZONE = "overrideTimezone"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String TBL_SETTING_PREFIX = "tbl."; //$NON-NLS-1$
    private static final String TBL_WIDTH = "w"; //$NON-NLS-1$
    private static final String TBL_ENABLE = "e"; //$NON-NLS-1$
    private static final String TBL_ORDER = ".order"; //$NON-NLS-1$

    private Locale selectedLocale;

    private Locale overrideLocale;

    private Locale overrideDateLocale;

    private DateTimeZone overrideTimezone;

    private Map<String, String> tblSettings = new HashMap<>();


    @PostConstruct
    protected void init () {
        if ( isAuthenticated() ) {
            try {
                this.ensureLoaded();
            }
            catch ( Exception e ) {
                log.trace("Failed to load preferences", e); //$NON-NLS-1$
                this.selectedLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                return;
            }
        }

        if ( this.overrideLocale != null ) {
            this.selectedLocale = this.overrideLocale;
        }
        else {
            this.selectedLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            if ( log.isDebugEnabled() ) {
                log.debug("Using auto-detected locale " + this.selectedLocale); //$NON-NLS-1$
            }
        }

    }


    /**
     * @return
     */
    protected abstract boolean isAuthenticated ();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#getOverrideDateLocale()
     */
    @Override
    public Locale getOverrideDateLocale () {
        return this.overrideDateLocale;
    }


    /**
     * @param overrideDateLocale
     *            the overrideDateLocale to set
     */
    public void setOverrideDateLocale ( Locale overrideDateLocale ) {
        this.overrideDateLocale = overrideDateLocale;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#getOverrideLocale()
     */
    @Override
    public Locale getOverrideLocale () {
        return this.overrideLocale;
    }


    /**
     * @param overrideLocale
     *            the overrideLocale to set
     */
    public void setOverrideLocale ( Locale overrideLocale ) {
        this.overrideLocale = overrideLocale;
        if ( overrideLocale != null ) {
            this.selectedLocale = overrideLocale;
        }
        else {
            this.selectedLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#getOverrideTimezone()
     */
    @Override
    public DateTimeZone getOverrideTimezone () {
        return this.overrideTimezone;
    }


    /**
     * @param overrideTimezone
     *            the overrideTimezone to set
     */
    public void setOverrideTimezone ( DateTimeZone overrideTimezone ) {
        this.overrideTimezone = overrideTimezone;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#getLocale()
     */
    @Override
    public Locale getLocale () {
        return this.selectedLocale;
    }


    /**
     * 
     */
    private void ensureLoaded () {

        if ( !isAuthenticated() ) {
            return;
        }

        this.loadPreferences();
    }


    /**
     * Reset the user's preferences
     * 
     * @return whether the preferences were reset
     */
    @Override
    public boolean resetPreferences () {
        boolean wasEmpty = toMap().isEmpty();
        Map<String, String> savePreferences = this.savePreferences(new HashMap<>());
        if ( savePreferences != null ) {
            fromMap(savePreferences);
            if ( !wasEmpty ) {
                preferencesChanged();
            }
            return true;
        }
        return false;
    }


    /**
     * @param hashMap
     * @return
     */
    protected abstract Map<String, String> savePreferences ( Map<String, String> hashMap );


    /**
     * Save the currently set preferences
     */
    @Override
    public boolean savePreferences () {
        Map<String, String> before = toMap();
        Map<String, String> savePreferences = savePreferences(before);
        if ( savePreferences != null ) {
            fromMap(savePreferences);
            if ( !savePreferences.equals(before) ) {
                preferencesChanged();
            }
            return true;
        }
        return false;
    }


    /**
     * 
     */
    protected void preferencesChanged () {

    }


    /**
     * Load the user's preferences
     */
    @Override
    public void loadPreferences () {
        Map<String, String> loadPreferencesInternal = loadPreferencesInternal();
        if ( loadPreferencesInternal != null ) {
            fromMap(loadPreferencesInternal);
        }
    }


    protected void trySave () {
        if ( !isAuthenticated() ) {
            return;
        }
        this.savePreferences();
    }


    /**
     * @return
     */
    protected abstract Map<String, String> loadPreferencesInternal ();


    /**
     * @param vals
     * @return
     */
    protected Map<String, String> toMap () {
        Map<String, String> vals = new HashMap<>();

        if ( this.overrideLocale != null ) {
            vals.put(OVERRIDE_LOCALE, this.overrideLocale.toLanguageTag());
        }

        if ( this.overrideDateLocale != null ) {
            vals.put(OVERRIDE_DATE_LOCALE, this.overrideDateLocale.toLanguageTag());
        }

        if ( this.overrideTimezone != null ) {
            vals.put(OVERRIDE_TIMEZONE, this.overrideTimezone.getID());
        }

        vals.putAll(this.tblSettings);

        return vals;
    }


    /**
     * @param vals
     */
    protected void fromMap ( Map<String, String> vals ) {

        if ( vals.containsKey(OVERRIDE_LOCALE) ) {
            this.overrideLocale = Locale.forLanguageTag(vals.get(OVERRIDE_LOCALE));
        }
        else {
            this.overrideLocale = null;
        }

        if ( vals.containsKey(OVERRIDE_DATE_LOCALE) ) {
            this.overrideDateLocale = Locale.forLanguageTag(vals.get(OVERRIDE_DATE_LOCALE));
        }
        else {
            this.overrideDateLocale = null;
        }

        if ( vals.containsKey(OVERRIDE_TIMEZONE) ) {
            this.overrideTimezone = DateTimeZone.forID(vals.get(OVERRIDE_TIMEZONE));
        }
        else {
            this.overrideTimezone = null;
        }

        Map<String, String> newTableSettings = new HashMap<>();

        for ( Entry<String, String> e : vals.entrySet() ) {
            if ( e.getKey().startsWith(TBL_SETTING_PREFIX) ) {
                newTableSettings.put(e.getKey(), e.getValue());
            }
        }

        this.tblSettings = newTableSettings;

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#getColumnWidth(java.lang.String, java.lang.String)
     */
    @Override
    public Integer getColumnWidth ( String tableName, String key ) {
        String val = this.tblSettings.get(makeColumnKey(tableName, key, TBL_WIDTH));
        if ( StringUtils.isBlank(val) ) {
            return null;
        }
        return Integer.valueOf(val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#setColumnWidth(java.lang.String, java.lang.String, int)
     */
    @Override
    public void setColumnWidth ( String tableName, String key, int width ) {
        this.tblSettings.put(makeColumnKey(tableName, key, TBL_WIDTH), String.valueOf(width));
        trySave();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#isColumnEnabled(java.lang.String, java.lang.String)
     */
    @Override
    public Boolean isColumnEnabled ( String tableName, String key ) {
        String val = this.tblSettings.get(makeColumnKey(tableName, key, TBL_ENABLE));
        if ( StringUtils.isBlank(val) ) {
            return null;
        }
        return Boolean.valueOf(val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#setColumnEnabled(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void setColumnEnabled ( String tableName, String key, boolean enable ) {
        String columnKey = makeColumnKey(tableName, key, TBL_ENABLE);
        this.tblSettings.put(columnKey, String.valueOf(enable));
    }


    /**
     * @param tableName
     * @param key
     * @return
     */
    private static String makeColumnKey ( String tableName, String key, String type ) {
        StringBuilder sb = new StringBuilder();
        sb.append(TBL_SETTING_PREFIX);
        sb.append(tableName);
        sb.append('.');
        sb.append(key);
        sb.append(type);
        return sb.toString();
    }


    /**
     * @param tableName
     * 
     */
    @Override
    public void resetColumnWidths ( String tableName ) {
        Set<String> remove = new HashSet<>();
        String prefix = TBL_SETTING_PREFIX + tableName + "."; //$NON-NLS-1$
        for ( String key : this.tblSettings.keySet() ) {
            if ( key.startsWith(prefix) && key.endsWith("." + TBL_WIDTH) ) { //$NON-NLS-1$
                remove.add(key);
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Removing " + remove); //$NON-NLS-1$
        }
        for ( String toRemove : remove ) {
            this.tblSettings.remove(toRemove);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#getColumnOrder(java.lang.String, java.util.List)
     */
    @Override
    public List<String> getColumnOrder ( String tableName, List<String> defaultOrder ) {
        String order = this.tblSettings.get(TBL_SETTING_PREFIX + tableName + TBL_ORDER);
        if ( order == null ) {
            return new ArrayList<>(defaultOrder);
        }
        return new ArrayList<>(Arrays.asList(StringUtils.split(order, ',')));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.UserPreferences#setColumnOrder(java.lang.String, java.util.List)
     */
    @Override
    public void setColumnOrder ( String tableName, List<String> order ) {
        this.tblSettings.put(TBL_SETTING_PREFIX + tableName + TBL_ORDER, StringUtils.join(order, ','));
    }
}
