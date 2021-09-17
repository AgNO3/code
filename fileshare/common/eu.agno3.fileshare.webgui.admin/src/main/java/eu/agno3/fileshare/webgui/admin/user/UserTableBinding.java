/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;


/**
 * @author mbechler
 *
 */
@RequestScoped
@Named ( "app_fs_adm_userTableBinding" )
public class UserTableBinding {

    private DataTable component;


    /**
     * @return the component
     */
    public DataTable getComponent () {
        return this.component;
    }


    /**
     * @param component
     *            the component to set
     */
    public void setComponent ( DataTable component ) {
        this.component = component;
    }
}
