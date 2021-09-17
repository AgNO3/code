/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.objects;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "objectController" )
@ApplicationScoped
public class ObjectController {

    @Inject
    private StructureViewContextBean viewContext;


    public String addTemplate () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return "/config/template/add.xhtml?faces-redirect=true&cid=&object=" + this.viewContext.getSelectedObject().getId(); //$NON-NLS-1$
    }


    public String getDisplayTypeName ( Class<? extends ConfigurationObject> clazz ) {
        ObjectTypeName annot = clazz.getAnnotation(ObjectTypeName.class);
        return annot.value();
    }
}
