/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.12.2013 by mbechler
 */
package org.primefaces.fixed.component.datatable;


import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.model.DataModel;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.datatable.feature.CellEditFeature;
import org.primefaces.component.datatable.feature.DataTableFeature;
import org.primefaces.component.datatable.feature.DataTableFeatureKey;
import org.primefaces.component.datatable.feature.DraggableColumnsFeature;
import org.primefaces.component.datatable.feature.FilterFeature;
import org.primefaces.component.datatable.feature.PageFeature;
import org.primefaces.component.datatable.feature.ResizableColumnsFeature;
import org.primefaces.component.datatable.feature.RowEditFeature;
import org.primefaces.component.datatable.feature.ScrollFeature;
import org.primefaces.component.datatable.feature.SelectionFeature;
import org.primefaces.component.datatable.feature.SortFeature;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;


/**
 * @author mbechler
 * 
 */
@ResourceDependencies ( {
    @ResourceDependency ( library = "primefaces", name = "components.css" ),
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery.js" ),
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery-plugins.js" ),
    @ResourceDependency ( library = "primefaces", name = "core.js" ), @ResourceDependency ( library = "primefaces", name = "components.js" ),
    @ResourceDependency ( library = "agno3", name = "library.js" )
})
public class DataTableFixed extends org.primefaces.component.datatable.DataTable {

    static Map<DataTableFeatureKey, DataTableFeature> FEATURES;


    static {
        FEATURES = new EnumMap<>(DataTableFeatureKey.class);
        FEATURES.put(DataTableFeatureKey.DRAGGABLE_COLUMNS, new DraggableColumnsFeature());
        FEATURES.put(DataTableFeatureKey.FILTER, new FilterFeature());
        FEATURES.put(DataTableFeatureKey.PAGE, new PageFeature());
        FEATURES.put(DataTableFeatureKey.SORT, new SortFeature());
        FEATURES.put(DataTableFeatureKey.RESIZABLE_COLUMNS, new ResizableColumnsFeature());
        FEATURES.put(DataTableFeatureKey.SELECT, new SelectionFeature());
        FEATURES.put(DataTableFeatureKey.ROW_EDIT, new RowEditFeature());
        FEATURES.put(DataTableFeatureKey.CELL_EDIT, new CellEditFeature());
        FEATURES.put(DataTableFeatureKey.ROW_EXPAND, new RowExpandFeatureFixed());
        FEATURES.put(DataTableFeatureKey.SCROLL, new ScrollFeature());
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.component.datatable.DataTable#getFeature(org.primefaces.component.datatable.feature.DataTableFeatureKey)
     */
    @Override
    public DataTableFeature getFeature ( DataTableFeatureKey key ) {
        return FEATURES.get(key);
    }


    /**
     * 
     * @param expandedRowIndex
     * @param rowKey
     */
    public void expandRow ( int expandedRowIndex, String rowKey ) {
        DataModel<?> model = getDataModel();

        if ( model instanceof LazyDataModel ) {
            setFirst(expandedRowIndex);
            int origRows = getRows();
            setRows(1);
            if ( !StringUtils.isBlank(rowKey) ) {
                loadLazyDataItem(rowKey);
            }
            else {
                loadLazyData();
            }
            setRows(origRows);
            setRowIndex(0);
        }
        else {
            setRowIndex(expandedRowIndex);
        }
    }


    /**
     * @param rowKey
     */
    @SuppressWarnings ( "rawtypes" )
    private void loadLazyDataItem ( String rowKey ) {
        DataModel model = getDataModel();

        if ( model != null && model instanceof LazyDataModel ) {
            LazyDataModel lazyModel = (LazyDataModel) model;

            List<?> data = null;

            // #7176
            calculateFirst();

            data = Arrays.asList(lazyModel.getRowData(rowKey));

            lazyModel.setPageSize(getRows());
            lazyModel.setWrappedData(data);

            // Update paginator for callback
            if ( this.isPaginator() ) {
                RequestContext requestContext = RequestContext.getCurrentInstance();

                if ( requestContext != null ) {
                    requestContext.addCallbackParam("totalRecords", lazyModel.getRowCount()); //$NON-NLS-1$
                }
            }
        }
    }


    @Override
    public void setRowIndex ( int rowIndex ) {
        super.setRowIndex(rowIndex);
        if ( isRowStatePreserved() ) {
            String rowIndexVar = this.getRowIndexVar();
            Map<String, Object> requestMap = getFacesContext().getExternalContext().getRequestMap();

            if ( rowIndex == -1 ) {
                if ( rowIndexVar != null ) {
                    requestMap.remove(rowIndexVar);
                }
            }
            else {
                if ( rowIndexVar != null ) {
                    requestMap.put(rowIndexVar, rowIndex);
                }
            }

            // reset all client ids, something (e.g. extval) might call getClientId somewhere and that result will be
            // improperly cached
            resetClientIds(this);
        }
    }


    /**
     * @param dataTableFixed
     */
    private void resetClientIds ( UIComponent parent ) {
        Iterator<UIComponent> it = parent.getFacetsAndChildren();
        while ( it.hasNext() ) {
            UIComponent c = it.next();
            c.setId(c.getId());
            resetClientIds(c);
        }
    }

}
