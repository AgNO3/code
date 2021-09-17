/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.UsageService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
public class DefaultForStructureDataModelBean extends AbstractStructureUsageDataModelBean {

    /**
     * 
     */
    private static final long serialVersionUID = 7963373560913041115L;


    /**
     * @param ssp
     * @param object
     */
    public DefaultForStructureDataModelBean ( ServerServiceProvider ssp, ConfigurationObject object ) {
        super("defaultFor", ssp, object); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.usage.AbstractUsageDataModelBean#createModel(java.io.Serializable)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    protected Set<StructuralObject> createModel ( ConfigurationObject obj ) throws GuiWebServiceException, AbstractModelException {
        return (Set<StructuralObject>) this.getSsp().getService(UsageService.class).getDefaultForStructure(obj);
    }

}
