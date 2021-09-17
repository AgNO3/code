/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.10.2015 by mbechler
 */
package org.primefaces.fixed.component.treetable;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.treetable.TreeTable;


/**
 * @author mbechler
 *
 */
@ResourceDependencies ( {
    @ResourceDependency ( library = "primefaces", name = "components.css" ),
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery.js" ), @ResourceDependency ( library = "primefaces", name = "core.js" ),
    @ResourceDependency ( library = "primefaces", name = "components.js" ), @ResourceDependency ( library = "agno3", name = "library.js" )
})
public class TreeTableFixed extends TreeTable {

    private static final Logger log = Logger.getLogger(TreeTableFixed.class);

    private static final Serializable PREFIX_COLUMNS = "prefixColumns"; //$NON-NLS-1$
    private static final Serializable SUFFIX_COLUMNS = "suffixColumns"; //$NON-NLS-1$
    private static final Serializable COLUMN_ORDER = "columnOrder"; //$NON-NLS-1$

    private static final Serializable PROPAGATE_SELECTION_UP = "propagateSelectionUp"; //$NON-NLS-1$
    private static final Serializable PROPAGATE_SELECTION_DOWN = "propagateSelectionDown"; //$NON-NLS-1$

    private transient List<UIColumn> ordered;


    /**
     * 
     * @return fixed prefix columns
     */
    public List<String> getPrefixColumnsList () {
        return Arrays.asList(StringUtils.split((String) this.getStateHelper().eval(PREFIX_COLUMNS), ", ")); //$NON-NLS-1$
    }


    /**
     * @return the prefixColumns
     */
    public String getPrefixColumns () {
        return (String) this.getStateHelper().get(PREFIX_COLUMNS);
    }


    /**
     * 
     * @param columns
     */
    public void setPrefixColumns ( String columns ) {
        this.getStateHelper().put(PREFIX_COLUMNS, columns);
    }


    /**
     * 
     * @return fixed suffix columns
     */
    public List<String> getSuffixColumnsList () {
        return Arrays.asList(StringUtils.split((String) this.getStateHelper().eval(SUFFIX_COLUMNS), ", ")); //$NON-NLS-1$
    }


    /**
     * @return the suffixColumns
     */
    public String getSuffixColumns () {
        return (String) this.getStateHelper().get(SUFFIX_COLUMNS);
    }


    /**
     * 
     * @param columns
     */
    public void setSuffixColumns ( String columns ) {
        this.getStateHelper().put(SUFFIX_COLUMNS, columns);
    }


    /**
     * 
     * @return columns order
     */
    @SuppressWarnings ( "unchecked" )
    public List<String> getColumnOrder () {
        return (List<String>) this.getStateHelper().eval(COLUMN_ORDER);
    }


    /**
     * 
     * @return whether to propagate selections up (true|false)
     */
    public String getPropagateSelectionUp () {
        return (String) this.getStateHelper().eval(PROPAGATE_SELECTION_UP, "true"); //$NON-NLS-1$
    }


    /**
     * 
     * @param propagateUp
     */
    public void setPropagateSelectionUp ( String propagateUp ) {
        this.getStateHelper().put(PROPAGATE_SELECTION_UP, propagateUp);
    }


    /**
     * 
     * @return whether to propagate selections up (true|select|deselect|false)
     */

    public String getPropagateSelectionDown () {
        return (String) this.getStateHelper().eval(PROPAGATE_SELECTION_DOWN, "true"); //$NON-NLS-1$
    }


    /**
     * 
     * @param propagateDown
     */
    public void setPropagateSelectionDown ( String propagateDown ) {
        this.getStateHelper().put(PROPAGATE_SELECTION_DOWN, propagateDown);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.treetable.TreeTable#getColumns()
     */
    @Override
    public List<UIColumn> getColumns () {

        if ( this.ordered != null ) {
            return this.ordered;
        }

        List<String> columnOrder = getColumnOrder();

        if ( columnOrder == null ) {
            return super.getColumns();
        }

        log.debug("Ordering columns"); //$NON-NLS-1$

        List<UIColumn> toOrder = new ArrayList<>(super.getColumns());
        Collections.sort(toOrder, new ColumnComparator(getPrefixColumnsList(), columnOrder, getSuffixColumnsList()));
        this.ordered = toOrder;
        return toOrder;
    }

}
