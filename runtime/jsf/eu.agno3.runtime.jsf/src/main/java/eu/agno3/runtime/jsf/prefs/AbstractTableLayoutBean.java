/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.prefs;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.ColumnResizeEvent;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;


/**
 * @author mbechler
 *
 */
public abstract class AbstractTableLayoutBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2428720558112710211L;
    private static final Logger log = Logger.getLogger(AbstractTableLayoutBean.class);


    protected abstract Map<String, Boolean> getDefaultEnable ();


    protected abstract Map<String, Integer> getDefaultWidths ();


    /**
     * 
     * @return the defined column keys
     */
    public abstract List<String> getDismissableColumns ();


    /**
     * 
     * @return the defined column keys
     */
    public abstract List<String> getResizeableColumns ();


    protected abstract String getTableName ();

    @Inject
    private UserPreferences userPrefs;


    /**
     * 
     */
    public AbstractTableLayoutBean () {
        super();
    }


    /**
     * @return the total size of all columns
     */
    public int getTotalWidth () {
        int total = 0;

        for ( String key : getResizeableColumns() ) {
            if ( this.getDismissableColumns().contains(key) && !showColumn(key) ) {
                continue;
            }
            Integer columnWidthInteger = getColumnWidthInteger(key);
            if ( columnWidthInteger != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Adding %dpx for %s", columnWidthInteger, key)); //$NON-NLS-1$
                }
                total += columnWidthInteger;
            }
            else if ( log.isDebugEnabled() ) {
                log.info("No column width for " + key); //$NON-NLS-1$
            }
        }

        total += getStaticColumnWidth() + 2;
        if ( log.isDebugEnabled() ) {
            log.debug("Total width is " + total); //$NON-NLS-1$
        }
        return total;
    }


    /**
     * @return the width of static columns
     */
    protected int getStaticColumnWidth () {
        return 0;
    }


    /**
     * 
     * @param ev
     */
    public void onColumnResize ( ColumnResizeEvent ev ) {
        String columnKey = ev.getColumn().getColumnKey();
        int lastSep = columnKey.lastIndexOf(':');
        if ( lastSep >= 0 ) {
            columnKey = columnKey.substring(lastSep + 1);
        }

        this.userPrefs.setColumnWidth(getTableName(), columnKey, Math.max(50, ev.getWidth()));
    }


    /**
     * 
     * @param key
     * @return the stored or default column width
     */
    public String getColumnWidth ( String key ) {
        Integer columnWidthInteger = getColumnWidthInteger(key);
        if ( columnWidthInteger != null ) {
            return columnWidthInteger + "px"; //$NON-NLS-1$
        }
        return "auto"; //$NON-NLS-1$
    }


    /**
     * @param key
     */
    protected Integer getColumnWidthInteger ( String key ) {
        Integer overrideWith = this.userPrefs.getColumnWidth(getTableName(), key);
        if ( overrideWith != null ) {
            return overrideWith; // $NON-NLS-1$
        }

        Integer defaultWidth = getDefaultWidths().get(key);
        if ( defaultWidth != null ) {
            return defaultWidth;
        }

        return null;
    }


    /**
     * @param key
     * @return whether to show column
     */
    public boolean showColumn ( String key ) {

        if ( !this.isColumnEnabledDynamic(key) ) {
            return false;
        }

        Boolean enabled = this.userPrefs.isColumnEnabled(getTableName(), key);

        if ( enabled != null ) {
            return enabled;
        }

        Boolean defaultEnabled = getDefaultEnable().get(key);
        if ( defaultEnabled != null ) {
            return defaultEnabled;
        }

        return true;
    }


    /**
     * 
     * @param key
     * @return whether the column is shown but has not been explicitly enabled by the user
     */
    public boolean isDefaultEnabled ( String key ) {
        if ( !this.isColumnEnabledDynamic(key) ) {
            return false;
        }

        Boolean enabled = this.userPrefs.isColumnEnabled(getTableName(), key);

        if ( enabled != null ) {
            return false;
        }

        if ( !this.getDismissableColumns().contains(key) ) {
            return false;
        }

        Boolean defaultEnabled = getDefaultEnable().get(key);
        if ( defaultEnabled != null ) {
            return defaultEnabled;
        }

        return true;
    }


    /**
     * 
     * @param key
     * @return default-enabled if the column is enabled by default
     */
    public String getClassIfDefaultEnabled ( String key ) {
        if ( isDefaultEnabled(key) ) {
            return "default-enabled"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * @param key
     * @return
     */
    protected boolean isColumnEnabledDynamic ( String key ) {
        return true;
    }


    /**
     * 
     * @return the keys of all enabled columns
     */
    public List<String> getEnabledColumns () {
        List<String> enabled = new ArrayList<>();

        for ( String key : this.getDismissableColumns() ) {
            if ( this.showColumn(key) ) {
                enabled.add(key);
            }
        }

        return enabled;
    }


    /**
     * @param enabled
     * 
     */
    public void setEnabledColumns ( List<String> enabled ) {
        // ignore
    }


    /**
     * 
     * @param ev
     */
    public void columnSelect ( SelectEvent ev ) {
        Object s = ev.getObject();
        if ( ! ( s instanceof String ) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Enable column " + ev.getObject()); //$NON-NLS-1$
        }
        this.clearColumnSizes();
        this.restoreColumn((String) ev.getObject());

    }


    /**
     * 
     */
    private void clearColumnSizes () {
        this.userPrefs.resetColumnWidths(getTableName());
    }


    /**
     * 
     * @param ev
     */
    public void columnUnselect ( UnselectEvent ev ) {
        Object s = ev.getObject();
        if ( ! ( s instanceof String ) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Hide column " + ev.getObject()); //$NON-NLS-1$
        }
        this.clearColumnSizes();
        this.hideColumn((String) ev.getObject());
    }


    /**
     * @param key
     */
    public void hideColumn ( String key ) {
        this.userPrefs.setColumnEnabled(getTableName(), key, false);
        this.userPrefs.savePreferences();

    }


    /**
     * @param key
     */
    public void restoreColumn ( String key ) {
        this.userPrefs.setColumnEnabled(getTableName(), key, true);
        this.userPrefs.savePreferences();
    }


    /**
     * 
     * @return the column order
     */
    public List<String> getColumnOrder () {
        return this.userPrefs.getColumnOrder(getTableName(), getDismissableColumns());
    }


    /**
     * 
     * @param order
     */
    public void setColumnOrder ( List<String> order ) {
        this.userPrefs.setColumnOrder(getTableName(), order);
        this.userPrefs.savePreferences();
    }


    /**
     * 
     * @param ev
     */
    public void reorderColumns ( ReorderEvent ev ) {
        int from = ev.getFromIndex();
        int to = ev.getToIndex();
        moveColumn(from, to);
    }


    /**
     * @param from
     * @param to
     */
    private void moveColumn ( int from, int to ) {

        if ( from == to ) {
            return;
        }

        List<String> oldOrder = getColumnOrder();

        String fromKey = oldOrder.get(from);
        String toKey = oldOrder.get(to);

        if ( log.isDebugEnabled() ) {
            log.debug("Old order is " + oldOrder); //$NON-NLS-1$
            log.debug(String.format("Reorder column %s (%d) -> %s (%d)", fromKey, from, toKey, to)); //$NON-NLS-1$
        }

        oldOrder.remove(from);
        oldOrder.add(to, fromKey);

        if ( log.isDebugEnabled() ) {
            log.debug("New order is " + oldOrder); //$NON-NLS-1$
        }

        setColumnOrder(oldOrder);
    }

}