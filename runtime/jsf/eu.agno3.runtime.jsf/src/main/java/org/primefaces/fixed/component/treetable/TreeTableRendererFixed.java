/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.01.2015 by mbechler
 */
package org.primefaces.fixed.component.treetable;


import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.primefaces.component.treetable.TreeTable;
import org.primefaces.component.treetable.TreeTableRenderer;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.primefaces.util.WidgetBuilder;


/**
 * 
 * Fix tree table selection when invalid row keys are passed
 * 
 * @author mbechler
 *
 */
public class TreeTableRendererFixed extends TreeTableRenderer {

    /**
     * 
     */
    private static final String SELECTION = "selection"; //$NON-NLS-1$

    private static final String SORTABLE_COLUMN_ICON_CLASS = "ui-sortable-column-icon ui-icon ui-icon-carat-2-n-s"; //$NON-NLS-1$
    private static final String SORTABLE_COLUMN_ASCENDING_ICON_CLASS = "ui-sortable-column-icon ui-icon ui-icon ui-icon-triangle-1-n"; //$NON-NLS-1$
    private static final String SORTABLE_COLUMN_DESCENDING_ICON_CLASS = "ui-sortable-column-icon ui-icon ui-icon ui-icon-triangle-1-s"; //$NON-NLS-1$


    @Override
    protected void decodeSelection ( FacesContext context, TreeTable tt ) {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String selectionMode = tt.getSelectionMode();

        // decode selection
        if ( selectionMode != null ) {
            String selectionValue = params.get(tt.getClientId(context) + "_selection"); //$NON-NLS-1$

            if ( !isValueBlank(selectionValue) ) {
                if ( selectionMode.equals("single") ) { //$NON-NLS-1$
                    tt.setRowKey(selectionValue);
                    TreeNode rowNode = tt.getRowNode();

                    if ( rowNode != null ) {
                        tt.setSelection(rowNode);
                    }
                }
                else {
                    String[] rowKeys = selectionValue.split(","); //$NON-NLS-1$
                    TreeNode[] selection = new TreeNode[rowKeys.length];

                    int j = 0;
                    for ( int i = 0; i < rowKeys.length; i++ ) {
                        tt.setRowKey(rowKeys[ i ]);

                        TreeNode rowNode = tt.getRowNode();
                        if ( rowNode != null ) {
                            selection[ j ] = rowNode;
                            j++;
                        }
                    }

                    tt.setSelection(Arrays.copyOf(selection, j));
                }

                tt.setRowKey(null); // cleanup
            }

            return;
        }

        super.decodeSelection(context, tt);
    }


    @Override
    @SuppressWarnings ( "nls" )
    protected void encodeScript ( FacesContext context, TreeTable tt ) throws IOException {
        TreeTableFixed ttf = ( tt instanceof TreeTableFixed ) ? (TreeTableFixed) tt : null;
        String clientId = tt.getClientId(context);
        String selectionMode = tt.getSelectionMode();
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.initWithDomReady("TreeTableFixed", tt.resolveWidgetVar(), clientId).attr("selectionMode", selectionMode, null)
                .attr("resizableColumns", tt.isResizableColumns(), false).attr("liveResize", tt.isLiveResize(), false)
                .attr("scrollable", tt.isScrollable(), false).attr("scrollHeight", tt.getScrollHeight(), null)
                .attr("scrollWidth", tt.getScrollWidth(), null).attr("nativeElements", tt.isNativeElements(), false)
                .attr("propagateSelectionUp", ttf != null ? ttf.getPropagateSelectionUp() : "true")
                .attr("propagateSelectionDown", ttf != null ? ttf.getPropagateSelectionDown() : "true");

        encodeClientBehaviors(context, tt);

        wb.finish();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.treetable.TreeTableRenderer#resolveSortIcon(javax.el.ValueExpression,
     *      javax.el.ValueExpression, java.lang.String)
     */
    @Override
    protected String resolveSortIcon ( ValueExpression columnSortBy, ValueExpression ttSortBy, String sortOrder ) {
        String columnSortByExpression = columnSortBy.getExpressionString();
        String ttSortByExpression = ttSortBy.getExpressionString();
        String sortIcon = null;

        if ( ttSortByExpression != null && ttSortByExpression.equals(columnSortByExpression) ) {
            if ( sortOrder.equalsIgnoreCase("ASCENDING") ) //$NON-NLS-1$
                sortIcon = SORTABLE_COLUMN_ASCENDING_ICON_CLASS;
            else if ( sortOrder.equalsIgnoreCase("DESCENDING") ) //$NON-NLS-1$
                sortIcon = SORTABLE_COLUMN_DESCENDING_ICON_CLASS;
        }
        else {
            sortIcon = SORTABLE_COLUMN_ICON_CLASS;
        }

        return sortIcon;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.treetable.TreeTableRenderer#encodeTbody(javax.faces.context.FacesContext,
     *      org.primefaces.component.treetable.TreeTable, boolean)
     */
    @Override
    protected void encodeTbody ( FacesContext context, TreeTable tt, boolean dataOnly ) throws IOException {
        TreeNode root = tt.getValue();
        synchronized ( root ) {
            super.encodeTbody(context, tt, dataOnly);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.treetable.TreeTableRenderer#sort(org.primefaces.component.treetable.TreeTable)
     */
    @Override
    public void sort ( TreeTable tt ) {
        TreeNode root = tt.getValue();
        if ( root == null ) {
            return;
        }

        synchronized ( root ) {
            super.sort(tt);

            if ( RequestContext.getCurrentInstance().getCallbackParams().get(SELECTION) == null ) {
                RequestContext.getCurrentInstance().getCallbackParams().remove(SELECTION);
            }
        }
    }
}
