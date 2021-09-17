/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.webbeans.util.StringUtil;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuItem;
import eu.agno3.orchestrator.server.webgui.structure.ServiceStateTracker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class BaseServiceMenuContributor extends AbstractMenuContributor {

    static final String MENU_SERVICE_MSG_PREFIX = "menu.service."; //$NON-NLS-1$

    @Inject
    private ServiceStateTracker serviceTracker;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject ) {
        return ( selectedObject instanceof ServiceStructuralObject && ( refObject == null ) ) || refObject instanceof ServiceStructuralObject;
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

        WeightedMenuItem itm;

        if ( !isHostService(selectedObject, refObject) ) {
            itm = new WeightedMenuItem(-100.0f, GuiMessages.get("menu.service.dashboard")); //$NON-NLS-1$
            itm.setOutcome("/structure/service/index.xhtml" + makeObjectParameters(selectedObject, refObject)); //$NON-NLS-1$
            res.add(itm);
        }

        itm = new WeightedMenuItem(0.0f, GuiMessages.get("menu.service.config")); //$NON-NLS-1$
        itm.setOutcome("/structure/service/config.xhtml" + makeObjectParameters(selectedObject, refObject)); //$NON-NLS-1$
        res.add(itm);

        return res;
    }


    private static boolean isHostService ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( selectedObject instanceof ServiceStructuralObject && refObject == null ) {
            return HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(selectedObject.getLocalType());
        }
        else if ( refObject instanceof ServiceStructuralObject ) {
            return HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(refObject.getLocalType());
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor#getListenTo(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public Collection<String> getListenTo ( StructuralObject selectedObject, StructuralObject refObject ) {
        ServiceStructuralObject service;
        Collection<String> listenTo = new HashSet<>();
        if ( selectedObject instanceof ServiceStructuralObject ) {
            service = (ServiceStructuralObject) selectedObject;
        }
        else if ( selectedObject instanceof InstanceStructuralObject && refObject instanceof ServiceStructuralObject ) {
            service = (ServiceStructuralObject) refObject;
        }
        else {
            return Collections.EMPTY_SET;
        }
        listenTo.add("/service/" + service.getId() + "/(.*)_status"); //$NON-NLS-1$ //$NON-NLS-2$
        return listenTo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor#notifyRefresh(java.lang.String,
     *      java.lang.String, eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean notifyRefresh ( String path, String payload, StructuralObject selectedObject, StructuralObject refObject ) {
        boolean changed = false;
        if ( StringUtil.isBlank(path) || ( path.startsWith("/service/") && path.endsWith("/runtime_status") ) ) { //$NON-NLS-1$//$NON-NLS-2$
            if ( selectedObject instanceof ServiceStructuralObject ) {
                this.serviceTracker.forceRefresh((ServiceStructuralObject) selectedObject);
                changed = true;
            }
            else if ( selectedObject instanceof InstanceStructuralObject && refObject instanceof ServiceStructuralObject ) {
                this.serviceTracker.forceRefresh((ServiceStructuralObject) refObject);
                changed = true;
            }
        }

        return super.notifyRefresh(path, payload, selectedObject, refObject) || changed;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected String makeObjectParameters ( StructuralObject selectedObject, StructuralObject refObject ) {

        if ( refObject != null ) {
            return String.format("?faces-redirect=true&cid=&anchor=%s&service=%s", selectedObject.getId(), refObject.getId()); //$NON-NLS-1$
        }

        return "?faces-redirect=true&service=" + selectedObject.getId(); //$NON-NLS-1$
    }

}
