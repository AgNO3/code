/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;


/**
 * @author mbechler
 * 
 */
@Named ( "serviceAddContext" )
@ViewScoped
public class ServiceAddContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6703827374312257115L;

    private ServiceStructuralObjectImpl newService;


    @PostConstruct
    protected void init () {
        this.newService = new ServiceStructuralObjectImpl();
    }


    /**
     * @return the newCluster
     */
    @Valid
    public ServiceStructuralObject getNewService () {
        return this.newService;
    }

}
