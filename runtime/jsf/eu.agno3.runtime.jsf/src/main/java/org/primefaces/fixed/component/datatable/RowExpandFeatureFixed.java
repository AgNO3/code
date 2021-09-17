/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.12.2013 by mbechler
 */
package org.primefaces.fixed.component.datatable;


import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableRenderer;
import org.primefaces.component.rowexpansion.RowExpansion;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "nls" )
public class RowExpandFeatureFixed extends org.primefaces.component.datatable.feature.RowExpandFeature {

    private static final Logger log = Logger.getLogger(RowExpandFeatureFixed.class);


    /**
     * 
     */
    public RowExpandFeatureFixed () {}


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.component.datatable.feature.RowExpandFeature#shouldDecode(javax.faces.context.FacesContext,
     *      org.primefaces.component.datatable.DataTable)
     */
    @Override
    public boolean shouldDecode ( FacesContext context, org.primefaces.component.datatable.DataTable table ) {
        return super.shouldDecode(context, table);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.component.datatable.feature.RowExpandFeature#shouldEncode(javax.faces.context.FacesContext,
     *      org.primefaces.component.datatable.DataTable)
     */
    @Override
    public boolean shouldEncode ( FacesContext context, org.primefaces.component.datatable.DataTable table ) {
        log.debug("shouldEncode()");
        return super.shouldEncode(context, table);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.datatable.feature.RowExpandFeature#encode(javax.faces.context.FacesContext,
     *      org.primefaces.component.datatable.DataTableRenderer, org.primefaces.component.datatable.DataTable)
     */
    @Override
    public void encode ( FacesContext context, DataTableRenderer renderer, DataTable table ) throws IOException {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String expandRowKey = params.get(table.getClientId(context) + "_expandedRowKey");
        int expandedRowIndex = Integer.parseInt(params.get(table.getClientId(context) + "_expandedRowIndex"));
        encodeExpansion(context, renderer, table, expandedRowIndex, expandRowKey);
        table.setRowIndex(-1);
    }


    protected void encodeExpansion ( FacesContext context, DataTableRenderer renderer, org.primefaces.component.datatable.DataTable table,
            int rowIndex, String rowKey ) throws IOException {
        @SuppressWarnings ( "resource" )
        ResponseWriter writer = context.getResponseWriter();
        String rowIndexVar = table.getRowIndexVar();
        RowExpansion rowExpansion = table.getRowExpansion();

        String styleClass = org.primefaces.component.datatable.DataTable.EXPANDED_ROW_CONTENT_CLASS + " ui-widget-content";
        if ( rowExpansion.getStyleClass() != null ) {
            styleClass = styleClass + " " + rowExpansion.getStyleClass();
        }

        if ( table instanceof org.primefaces.fixed.component.datatable.DataTableFixed ) {
            DataTableFixed dataTableFixed = (org.primefaces.fixed.component.datatable.DataTableFixed) table;
            dataTableFixed.expandRow(rowIndex, rowKey);
        }

        table.setRowIndex(rowIndex);

        if ( rowExpansion.isRendered() ) {
            if ( rowIndexVar != null ) {
                context.getExternalContext().getRequestMap().put(rowIndexVar, rowIndex);
            }

            writer.startElement("tr", null);
            writer.writeAttribute("class", styleClass, null);

            writer.startElement("td", null);
            writer.writeAttribute("colspan", table.getColumnsCount(), null);

            table.getRowExpansion().encodeAll(context);

            writer.endElement("td");

            writer.endElement("tr");
        }
    }
}
