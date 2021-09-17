/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.AbstractStructuralActionMenuContributor;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContribution;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class ServiceActionMenuContributor extends AbstractStructuralActionMenuContributor implements ActionMenuContributor {

    private static final String SERVICE_CONTEXT_CONTROLLER = "serviceContextMenuController"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObjectType,
     *      boolean)
     */
    @Override
    public boolean isApplicable ( StructuralObjectType type, boolean withContext ) {
        return type == StructuralObjectType.SERVICE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.AbstractStructuralActionMenuContributor#getControllerName()
     */
    @Override
    protected String getControllerName () {
        return SERVICE_CONTEXT_CONTROLLER;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#getContributions(boolean)
     */
    @Override
    public Set<ActionMenuContribution> getContributions ( boolean fromContext ) {
        Set<ActionMenuContribution> contributions = new HashSet<>();
        makeContribution(fromContext, contributions, "configure",//$NON-NLS-1$ 
            -50.0f,
            "ui-icon-wrench"); //$NON-NLS-1$
        makeContribution(fromContext, contributions, "deleteService",//$NON-NLS-1$ 
            100.0f,
            "ui-icon-trash"); //$NON-NLS-1$
        return contributions;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#getLabelKeyPrefix()
     */
    @Override
    public String getLabelKeyPrefix () {
        return "service.action."; //$NON-NLS-1$
    }

}
