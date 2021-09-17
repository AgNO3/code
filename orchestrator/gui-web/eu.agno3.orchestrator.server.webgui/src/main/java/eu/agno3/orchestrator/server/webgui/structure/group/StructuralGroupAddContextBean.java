/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.group;


import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObjectImpl;


/**
 * @author mbechler
 * 
 */
@Named ( "structuralGroupAddContext" )
@ViewScoped
public class StructuralGroupAddContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6703827374312257115L;

    private GroupStructuralObjectImpl newGroup;


    @PostConstruct
    protected void init () {
        this.newGroup = new GroupStructuralObjectImpl();
    }


    /**
     * @return the newCluster
     */
    @Valid
    public GroupStructuralObject getNewGroup () {
        return this.newGroup;
    }

}
