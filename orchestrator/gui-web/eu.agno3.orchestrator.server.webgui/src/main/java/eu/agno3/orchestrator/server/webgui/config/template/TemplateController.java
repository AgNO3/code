/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.template;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * 
 */
@Named ( "templateController" )
@ApplicationScoped
public class TemplateController {

    public String addTemplate ( StructuralObject anchor ) {
        return String.format("/config/template/add?faces-redirect=true&cid=&anchor=%s",//$NON-NLS-1$
            anchor.getId());
    }


    public String editTemplate ( StructuralObject anchor, ConfigurationObject obj ) {
        return String.format("/config/template/edit?faces-redirect=true&cid=&anchor=%s&object=%s",//$NON-NLS-1$
            anchor.getId(),
            obj.getId());
    }


    public String configureTemplate ( StructuralObject anchor, String objectTypeName ) {
        return String.format("/config/template/editNew?faces-redirect=true&cid=&anchor=%s&objectType=%s",//$NON-NLS-1$
            anchor.getId(),
            objectTypeName);
    }

}
