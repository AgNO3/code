/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.ui;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.TreeFilterBean;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;
import eu.agno3.runtime.jsf.prefs.AbstractTableLayoutBean;


/**
 * @author mbechler
 *
 */
@Named ( "fileTableLayoutBean" )
@SessionScoped
public class FileTableLayoutBean extends AbstractTableLayoutBean implements Serializable {

    private static final String TABLE_NAME = "files"; //$NON-NLS-1$

    private static final String OWNER_KEY = "owner"; //$NON-NLS-1$
    private static final String FILE_KEY = "file"; //$NON-NLS-1$
    private static final String SIZE_KEY = "size"; //$NON-NLS-1$
    private static final String TYPE_KEY = "type"; //$NON-NLS-1$
    private static final String LABEL_KEY = "label"; //$NON-NLS-1$
    private static final String EXPIRES_KEY = "expires"; //$NON-NLS-1$
    private static final String CREATED_KEY = "created"; //$NON-NLS-1$
    private static final String CREATOR_KEY = "creator"; //$NON-NLS-1$
    private static final String LAST_MODFR_KEY = "lastModifier"; //$NON-NLS-1$
    private static final String LAST_MOD_KEY = "lastModified"; //$NON-NLS-1$
    private static final String PEER_KEY = "peer"; //$NON-NLS-1$
    private static final String SHARE_KEY = "share"; //$NON-NLS-1$
    private static final String RESIZE_HELPER_KEY = "resizeHelper"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = 1372637251710156944L;

    @Inject
    private FileRootSelectionBean rootSelection;

    @Inject
    private TreeFilterBean treeFilter;

    private static final List<String> RESIZEABLE_COLUMNS = Arrays.asList(
        FILE_KEY,
        SIZE_KEY,
        TYPE_KEY,
        PEER_KEY,
        CREATOR_KEY,
        CREATED_KEY,
        LAST_MODFR_KEY,
        LAST_MOD_KEY,
        EXPIRES_KEY,
        LABEL_KEY,
        OWNER_KEY,
        SHARE_KEY,
        RESIZE_HELPER_KEY);

    private static final List<String> DISMISSABLE_COLUMNS = Arrays.asList(
        SIZE_KEY,
        TYPE_KEY,
        EXPIRES_KEY,
        CREATED_KEY,
        CREATOR_KEY,
        LAST_MOD_KEY,
        LAST_MODFR_KEY,
        LABEL_KEY,
        OWNER_KEY,
        PEER_KEY,
        SHARE_KEY);

    private static final Map<String, Integer> DEFAULT_WIDTHS = new HashMap<>();
    private static final Map<String, Boolean> DEFAULT_ENABLE = new HashMap<>();


    static {

        for ( String key : RESIZEABLE_COLUMNS ) {
            DEFAULT_WIDTHS.put(key, 200);
        }

        DEFAULT_WIDTHS.put(FILE_KEY, 450);
        DEFAULT_WIDTHS.put(SIZE_KEY, 80);
        DEFAULT_WIDTHS.put(PEER_KEY, 70);
        DEFAULT_WIDTHS.put(LAST_MOD_KEY, 150);

        DEFAULT_WIDTHS.put(RESIZE_HELPER_KEY, 5);

        // DEFAULT_WIDTHS.put(CREATED_KEY, "14%"); //$NON-NLS-1$
        DEFAULT_ENABLE.put(CREATED_KEY, false);
        // DEFAULT_WIDTHS.put(CREATOR_KEY, "15%"); //$NON-NLS-1$
        DEFAULT_ENABLE.put(CREATOR_KEY, false);
        // DEFAULT_WIDTHS.put(LAST_MODFR_KEY, "14%"); //$NON-NLS-1$
        DEFAULT_ENABLE.put(LAST_MODFR_KEY, false);
        // DEFAULT_WIDTHS.put(EXPIRES_KEY, "14%"); //$NON-NLS-1$
        DEFAULT_ENABLE.put(EXPIRES_KEY, false);
        // DEFAULT_WIDTHS.put(OWNER_KEY, "15%"); //$NON-NLS-1$
        DEFAULT_ENABLE.put(OWNER_KEY, false);
        // DEFAULT_WIDTHS.put(LABEL_KEY, "10%"); //$NON-NLS-1$
        DEFAULT_ENABLE.put(LABEL_KEY, false);
        // DEFAULT_WIDTHS.put(LAST_MOD_KEY, "14%"); //$NON-NLS-1$
        // DEFAULT_ENABLE.put(LAST_MOD_KEY, false);

        DEFAULT_ENABLE.put(TYPE_KEY, false);
    }


    /**
     * @return the columns
     */
    @Override
    public List<String> getDismissableColumns () {
        return DISMISSABLE_COLUMNS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractTableLayoutBean#getStaticColumnWidth()
     */
    @Override
    protected int getStaticColumnWidth () {
        return 5 + 24 + ( this.treeFilter.includesHidden() ? 24 : 0 );
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractTableLayoutBean#isColumnEnabledDynamic(java.lang.String)
     */
    @Override
    protected boolean isColumnEnabledDynamic ( String key ) {
        if ( PEER_KEY.equals(key) && !FileTreeConstants.PEERS_ROOT_TYPE.equals(this.rootSelection.getSelectedType()) ) {
            return false;
        }
        else if ( FileTreeConstants.PEERS_ROOT_TYPE.equals(this.rootSelection.getSelectedType()) ) {
            if ( !TYPE_KEY.equals(key) && !FILE_KEY.equals(key) && !PEER_KEY.equals(key) ) {
                return false;
            }
        }

        return super.isColumnEnabledDynamic(key);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractTableLayoutBean#getResizeableColumns()
     */
    @Override
    public List<String> getResizeableColumns () {
        return RESIZEABLE_COLUMNS;
    }


    @Override
    protected String getTableName () {
        return TABLE_NAME;
    }


    /**
     * @return
     */
    @Override
    protected Map<String, Integer> getDefaultWidths () {
        return DEFAULT_WIDTHS;
    }


    /**
     * @return
     */
    @Override
    protected Map<String, Boolean> getDefaultEnable () {
        return DEFAULT_ENABLE;
    }


    /**
     * 
     * @param columnId
     * @return filetable.columns. + columnId
     */
    public String getColumnKey ( String columnId ) {
        return "filetable.column." + columnId; //$NON-NLS-1$
    }
}
