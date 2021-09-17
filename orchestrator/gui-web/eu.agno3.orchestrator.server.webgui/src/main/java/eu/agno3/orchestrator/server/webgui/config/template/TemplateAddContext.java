/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.template;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 * 
 */
@Named ( "templateAddContext" )
@ViewScoped
public class TemplateAddContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8270776550533543854L;
    private String objectTypeName;


    /**
     * @return the objectTypeName
     */
    public String getObjectTypeName () {
        return this.objectTypeName;
    }


    /**
     * @param objectTypeName
     *            the objectTypeName to set
     */
    public void setObjectTypeName ( String objectTypeName ) {
        this.objectTypeName = objectTypeName;
    }

}
