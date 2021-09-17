/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.runtime.jsf.prefs;


import java.util.List;
import java.util.Locale;

import org.joda.time.DateTimeZone;


/**
 * @author mbechler
 *
 */
public interface UserPreferences {

    /**
     * @return locale override for date formats
     */
    Locale getOverrideDateLocale ();


    /**
     * @return override locale
     */
    Locale getOverrideLocale ();


    /**
     * @return the overrideTimezone
     */
    DateTimeZone getOverrideTimezone ();


    /**
     * @return the autoselected or overridden locale
     */
    Locale getLocale ();


    /**
     * @param tableName
     * @param key
     * @return the desired column width
     */
    Integer getColumnWidth ( String tableName, String key );


    /**
     * @param tableName
     * @param key
     * @param width
     */
    void setColumnWidth ( String tableName, String key, int width );


    /**
     * @param tableName
     * @param key
     * @return whether the column is enabled or not
     */
    Boolean isColumnEnabled ( String tableName, String key );


    /**
     * @param tableName
     * @param key
     * @param enable
     */
    void setColumnEnabled ( String tableName, String key, boolean enable );


    /**
     * @param tableName
     * @param defaultOrder
     * @return the column order
     */
    List<String> getColumnOrder ( String tableName, List<String> defaultOrder );


    /**
     * @param tableName
     * @param order
     */
    void setColumnOrder ( String tableName, List<String> order );


    /**
     * @param tableName
     */
    void resetColumnWidths ( String tableName );


    /**
     * @return whether the preferences were saved
     * 
     */
    boolean savePreferences ();


    /**
     * 
     */
    void loadPreferences ();


    /**
     * @return whether the preferences were reset
     */
    boolean resetPreferences ();

}