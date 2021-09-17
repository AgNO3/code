/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;


/**
 * @author mbechler
 *
 */
public class RoleDataModel extends ListDataModel<String> implements SelectableDataModel<String>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1617558920435213848L;


    /**
     * 
     */
    public RoleDataModel () {
        super();
    }


    /**
     * @param list
     */
    public RoleDataModel ( List<String> list ) {
        super(list);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.SelectableDataModel#getRowData(java.lang.String)
     */
    @Override
    public String getRowData ( String val ) {
        return val;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.SelectableDataModel#getRowKey(java.lang.Object)
     */
    @Override
    public Object getRowKey ( String val ) {
        return val;
    }

}
