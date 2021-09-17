/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiFeatureConfig;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuItem;
import eu.agno3.orchestrator.server.webgui.prefs.UserPreferencesBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class ObjectsMenuContributor extends AbstractMenuContributor {

    /**
     * 
     */
    private static final String STRUCTURE_OBJECTS_XHTML = "/structure/objects.xhtml"; //$NON-NLS-1$
    private static final String STRUCTURE_RESOURCE_LIBRARIES_XHTML = "/structure/resourceLibraries.xhtml"; //$NON-NLS-1$

    @Inject
    private GuiFeatureConfig guiFeatures;

    @Inject
    private UserPreferencesBean userPreferences;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( refObject != null ) {
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#getContributions(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public List<WeightedMenuElement> getContributions ( StructuralObject selectedObject, StructuralObject refObject ) {
        List<WeightedMenuElement> res = new ArrayList<>();

        if ( selectedObject instanceof ServiceStructuralObject && this.guiFeatures.getShowDevTools() ) {
            WeightedMenuItem itm = new WeightedMenuItem(10000.0f, GuiMessages.get("menu.structure.objects")); //$NON-NLS-1$
            itm.setOutcome(STRUCTURE_OBJECTS_XHTML + makeObjectParameters(selectedObject.getId()));
            res.add(itm);
        }
        else if ( selectedObject == null ) {
            return res;
        }
        else {
            if ( this.userPreferences.getEnableMultiHostManagement() && selectedObject instanceof GroupStructuralObject ) {
                WeightedMenuItem itm = new WeightedMenuItem(10000.0f, GuiMessages.get("menu.structure.templates")); //$NON-NLS-1$
                itm.setOutcome(STRUCTURE_OBJECTS_XHTML + makeObjectParameters(selectedObject.getId()));
                res.add(itm);

                if ( this.guiFeatures.getAllowDefaults() ) {
                    itm = new WeightedMenuItem(10002.0f, GuiMessages.get("menu.structure.defaults")); //$NON-NLS-1$
                    itm.setOutcome("/structure/defaults.xhtml" + makeObjectParameters(selectedObject.getId())); //$NON-NLS-1$
                    res.add(itm);
                }

                if ( this.guiFeatures.getAllowEnforcements() ) {
                    itm = new WeightedMenuItem(10003.0f, GuiMessages.get("menu.structure.enforcements")); //$NON-NLS-1$
                    itm.setOutcome("/structure/enforcements.xhtml" + makeObjectParameters(selectedObject.getId())); //$NON-NLS-1$
                    res.add(itm);
                }
            }
        }

        if ( this.userPreferences.getEnableMultiHostManagement()
                && ( selectedObject instanceof GroupStructuralObject || this.guiFeatures.getShowDevTools() ) ) {
            WeightedMenuItem itm = new WeightedMenuItem(10001.0f, GuiMessages.get("menu.structure.resourceLibraries")); //$NON-NLS-1$
            itm.setOutcome(STRUCTURE_RESOURCE_LIBRARIES_XHTML + makeObjectParameters(selectedObject.getId()));
            res.add(itm);
        }
        return res;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected String makeObjectParameters ( UUID objectID ) {
        return "?faces-redirect=true&cid=&anchor=" + objectID; //$NON-NLS-1$
    }
}
