/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.group;


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
public class GroupActionMenuContributor extends AbstractStructuralActionMenuContributor implements ActionMenuContributor {

    static final String GROUP_CONTEXT_CONTROLLER = "structuralGroupContextMenuController"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObjectType,
     *      boolean)
     */
    @Override
    public boolean isApplicable ( StructuralObjectType type, boolean withContext ) {
        return type == StructuralObjectType.GROUP;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.AbstractStructuralActionMenuContributor#getControllerName()
     */
    @Override
    protected String getControllerName () {
        return GROUP_CONTEXT_CONTROLLER;
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
        makeContribution(fromContext, contributions, "addTemplate",//$NON-NLS-1$ 
            -100.0f,
            "ui-icon-script"); //$NON-NLS-1$
        makeContribution(fromContext, contributions, "addGroup",//$NON-NLS-1$ 
            -50.0f,
            "ui-icon-folder-collapsed"); //$NON-NLS-1$
        makeContribution(fromContext, contributions, "addInstance", //$NON-NLS-1$
            -51.0f,
            "ui-icon-home"); //$NON-NLS-1$
        makeContribution(fromContext, contributions, "deleteGroup",//$NON-NLS-1$
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
        return "group.action."; //$NON-NLS-1$
    }

}
