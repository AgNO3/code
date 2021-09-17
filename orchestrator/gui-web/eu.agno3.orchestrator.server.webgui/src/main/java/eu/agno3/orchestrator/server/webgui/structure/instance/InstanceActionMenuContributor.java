/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.prefs.UserPreferencesBean;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.AbstractStructuralActionMenuContributor;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContribution;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class InstanceActionMenuContributor extends AbstractStructuralActionMenuContributor implements ActionMenuContributor {

    private static final String INSTANCE_CONTEXT_CONTROLLER = "instanceContextMenuController"; //$NON-NLS-1$

    @Inject
    private UserPreferencesBean userPrefs;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObjectType,
     *      boolean)
     */
    @Override
    public boolean isApplicable ( StructuralObjectType type, boolean withContext ) {
        return type == StructuralObjectType.INSTANCE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.AbstractStructuralActionMenuContributor#getControllerName()
     */
    @Override
    protected String getControllerName () {
        return INSTANCE_CONTEXT_CONTROLLER;
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
        makeContribution(
            fromContext,
            contributions,
            "addService", //$NON-NLS-1$
            -50.0f,
            "ui-icon-lightbulb"); //$NON-NLS-1$

        makeContribution(
            fromContext,
            contributions,
            "renameInstance", //$NON-NLS-1$
            -20.0f,
            "ui-icon-pencil"); //$NON-NLS-1$

        makeContribution(
            fromContext,
            contributions,
            "rebootInstance", //$NON-NLS-1$
            50.0f,
            "ui-icon-refresh"); //$NON-NLS-1$

        makeContribution(
            fromContext,
            contributions,
            "shutdownInstance", //$NON-NLS-1$
            50.5f,
            "ui-icon-power"); //$NON-NLS-1$

        makeContribution(
            fromContext,
            contributions,
            "changeShellPassword", //$NON-NLS-1$
            60.0f,
            "ui-icon-key"); //$NON-NLS-1$

        makeContribution(
            fromContext,
            contributions,
            "forceApplyConfig", //$NON-NLS-1$
            70.0f,
            "ui-icon-gear"); //$NON-NLS-1$

        if ( this.userPrefs.getEnableMultiHostManagement() ) {
            makeContribution(
                fromContext,
                contributions,
                "deleteInstance", //$NON-NLS-1$
                100.0f,
                "ui-icon-trash"); //$NON-NLS-1$
        }
        return contributions;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#getLabelKeyPrefix()
     */
    @Override
    public String getLabelKeyPrefix () {
        return "instance.action."; //$NON-NLS-1$
    }

}
