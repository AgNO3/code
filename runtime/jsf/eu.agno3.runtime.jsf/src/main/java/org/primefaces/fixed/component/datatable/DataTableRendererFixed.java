/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.12.2013 by mbechler
 */
package org.primefaces.fixed.component.datatable;


import java.io.IOException;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.feature.DataTableFeature;
import org.primefaces.util.Constants;
import org.primefaces.util.WidgetBuilder;


/**
 * @author mbechler
 * 
 */
public class DataTableRendererFixed extends org.primefaces.component.datatable.DataTableRenderer {

    @Override
    public void decode ( FacesContext context, UIComponent component ) {
        DataTableFixed table = (DataTableFixed) component;

        for ( Iterator<DataTableFeature> it = DataTableFixed.FEATURES.values().iterator(); it.hasNext(); ) {
            DataTableFeature feature = it.next();

            if ( feature.shouldDecode(context, table) ) {
                feature.decode(context, table);
            }
        }

        decodeBehaviors(context, component);
    }


    @Override
    @SuppressWarnings ( "nls" )
    protected void encodeNativeCheckbox ( FacesContext context, DataTable table, boolean checked, boolean disabled, boolean isHeaderCheckbox )
            throws IOException {
        @SuppressWarnings ( "resource" )
        ResponseWriter writer = context.getResponseWriter();

        String ariaRowLabel = table.getAriaRowLabel();

        writer.startElement("input", null);
        writer.writeAttribute("type", "checkbox", null);
        writer.writeAttribute("name", table.getClientId(context) + "_checkbox", null);
        writer.writeAttribute("aria-label", ariaRowLabel, null);
        writer.writeAttribute("aria-checked", String.valueOf(checked), null);

        if ( checked ) {
            writer.writeAttribute("checked", "checked", null);
        }

        if ( disabled ) {
            writer.writeAttribute("disabled", "disabled", null);
        }

        writer.endElement("input");
    }


    @Override
    @SuppressWarnings ( "nls" )
    protected void encodeScript ( FacesContext context, DataTable table ) throws IOException {
        String clientId = table.getClientId(context);
        String selectionMode = table.resolveSelectionMode();
        String widgetClass = ( table.getFrozenColumns() == 0 ) ? "DataTableFixed" : "FrozenDataTable";
        String initMode = table.getInitMode();

        WidgetBuilder wb = getWidgetBuilder(context);

        if ( initMode.equals("load") )
            wb.initWithDomReady(widgetClass, table.resolveWidgetVar(), clientId);
        else if ( initMode.equals("immediate") )
            wb.init(widgetClass, table.resolveWidgetVar(), clientId);
        else
            throw new FacesException(initMode + " is not a valid value for initMode, possible values are \"load\" and \"immediate.");

        // Pagination
        if ( table.isPaginator() ) {
            encodePaginatorConfig(context, table, wb);
        }

        // Selection
        wb.attr("selectionMode", selectionMode, null).attr("rowSelectMode", table.getRowSelectMode(), "new")
                .attr("nativeElements", table.isNativeElements(), false).attr("disabledTextSelection", table.isDisabledTextSelection(), true);

        // Filtering
        if ( table.isFilteringEnabled() ) {
            wb.attr("filter", true).attr("filterEvent", table.getFilterEvent(), null).attr("filterDelay", table.getFilterDelay(), Integer.MAX_VALUE);
        }

        // Row expansion
        if ( table.getRowExpansion() != null ) {
            wb.attr("expansion", true).attr("rowExpandMode", table.getRowExpandMode());
        }

        // Scrolling
        if ( table.isScrollable() ) {
            wb.attr("scrollable", true).attr("liveScroll", table.isLiveScroll()).attr("scrollStep", table.getScrollRows())
                    .attr("scrollLimit", table.getRowCount()).attr("scrollWidth", table.getScrollWidth(), null)
                    .attr("scrollHeight", table.getScrollHeight(), null).attr("frozenColumns", table.getFrozenColumns(), 0)
                    .attr("liveScrollBuffer", table.getLiveScrollBuffer());
        }

        // Resizable/Draggable Columns
        wb.attr("resizableColumns", table.isResizableColumns(), false).attr("liveResize", table.isLiveResize(), false)
                .attr("draggableColumns", table.isDraggableColumns(), false).attr("resizeMode", table.getResizeMode(), "fit");

        // Draggable Rows
        wb.attr("draggableRows", table.isDraggableRows(), false);

        // Editing
        if ( table.isEditable() ) {
            wb.attr("editable", true).attr("editMode", table.getEditMode()).attr("cellSeparator", table.getCellSeparator(), null);
        }

        // MultiColumn Sorting
        if ( table.isMultiSort() ) {
            wb.attr("multiSort", true);
        }

        if ( table.isStickyHeader() ) {
            wb.attr("stickyHeader", true);
        }

        wb.attr("tabindex", table.getTabindex(), null).attr("reflow", table.isReflow(), false).attr("rowHover", table.isRowHover(), false);

        // Behaviors
        encodeClientBehaviors(context, table);

        wb.finish();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.datatable.DataTableRenderer#encodeMarkup(javax.faces.context.FacesContext,
     *      org.primefaces.component.datatable.DataTable)
     */
    @Override
    protected void encodeMarkup ( FacesContext context, DataTable table ) throws IOException {
        String helperKey = (String) context.getAttributes().get(Constants.HELPER_RENDERER);
        try {
            context.getAttributes().remove(Constants.HELPER_RENDERER);
            super.encodeMarkup(context, table);
        }
        finally {
            if ( helperKey != null ) {
                context.getAttributes().put(Constants.HELPER_RENDERER, helperKey);
            }
        }
    }


    @Override
    public void encodeEnd ( FacesContext context, UIComponent component ) throws IOException {
        DataTableFixed table = (DataTableFixed) component;

        if ( table.shouldEncodeFeature(context) ) {
            for ( Iterator<DataTableFeature> it = DataTableFixed.FEATURES.values().iterator(); it.hasNext(); ) {
                DataTableFeature feature = it.next();

                if ( feature.shouldEncode(context, table) ) {
                    feature.encode(context, this, table);
                }
            }
        }
        else {
            preRender(context, table);

            encodeMarkup(context, table);
            encodeScript(context, table);
        }
    }

}
