/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.UsageService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
public class AffectsDataModelBean extends AbstractObjectUsageDataModelBean {

    /**
     * 
     */
    private static final long serialVersionUID = 8899287783554800019L;


    /**
     * @param ssp
     * @param obj
     * 
     */
    public AffectsDataModelBean ( ServerServiceProvider ssp, ConfigurationObject obj ) {
        super("affects", ssp, obj); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.usage.AbstractUsageDataModelBean#createModel(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected Set<ConfigurationObject> createModel ( ConfigurationObject obj ) throws GuiWebServiceException, ModelObjectNotFoundException,
            ModelServiceException {
        return this.getSsp().getService(UsageService.class).getAffects(obj);
    }

}
