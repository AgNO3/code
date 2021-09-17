/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "storageCreateFilesystemProperties" )
public class FilesystemProperties implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8483413638592215017L;

    private String label;


    /**
     * @return the label
     */
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }
}
