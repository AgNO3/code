/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "structureViewController" )
public class StructureViewController {

    public String group ( GroupStructuralObject obj ) {
        return "/structure/group/index.xhtml?faces-redirect=true&group=" + obj.getId(); //$NON-NLS-1$
    }


    public String instance ( InstanceStructuralObject obj ) {
        return "/structure/instance/index.xhtml?faces-redirect=true&instance=" + obj.getId(); //$NON-NLS-1$
    }


    public String service ( ServiceStructuralObject obj ) {
        return "/structure/service/index.xhtml?faces-redirect=true&service=" + obj.getId(); //$NON-NLS-1$
    }


    public String object ( StructuralObject obj ) {
        switch ( obj.getType() ) {
        case GROUP:
            return this.group((GroupStructuralObject) obj);
        case INSTANCE:
            return this.instance((InstanceStructuralObject) obj);
        case SERVICE:
            return this.service((ServiceStructuralObject) obj);
        default:
            throw new FacesException("Unknown structural object type " + obj.getType()); //$NON-NLS-1$
        }

    }
}
