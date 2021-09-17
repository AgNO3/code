/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree.ui;


import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.primefaces.component.treetable.TreeTable;


/**
 * @author mbechler
 *
 */
@RequestScoped
@Named ( "fileTreeComponentBinding" )
public class FileTreeComponentBinding {

    private TreeTable component;


    /**
     * @return the component
     */
    public TreeTable getComponent () {
        return this.component;
    }


    /**
     * @param component
     *            the component to set
     */
    public void setComponent ( TreeTable component ) {
        this.component = component;
    }
}
