/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "usageDataModels" )
@ViewScoped
public class UsageDataModelsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -361912704772203309L;

    private UUID configId;

    private ConfigurationObject cfg;

    @Inject
    private ServerServiceProvider ssp;

    private List<AbstractObjectUsageDataModelBean> objectModels;

    private List<AbstractUsageDataModelBean<? extends StructuralObject>> serviceModels;


    /**
     * @return the configId
     */
    public UUID getConfigId () {
        return this.configId;
    }


    /**
     * @param configId
     *            the configId to set
     */
    public void setConfigId ( UUID configId ) {
        this.configId = configId;
    }


    /**
     * @return the selected configuration
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public ConfigurationObject getConfiguration () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.cfg == null ) {
            this.cfg = this.ssp.getService(ConfigurationService.class).fetchById(this.configId);
        }
        return this.cfg;
    }


    public List<AbstractObjectUsageDataModelBean> getObjectModels () throws ModelObjectNotFoundException, ModelServiceException,
            GuiWebServiceException {

        if ( this.objectModels == null ) {
            this.objectModels = new LinkedList<>();
            ConfigurationObject configuration = this.getConfiguration();
            this.objectModels.add(new AffectsDataModelBean(this.ssp, configuration));
            this.objectModels.add(new AffectedByDataModelBean(this.ssp, configuration));
            this.objectModels.add(new ClosureUsedByDataModelBean(this.ssp, configuration));
            this.objectModels.add(new ClosureUsesDataModelBean(this.ssp, configuration));
            this.objectModels.add(new DefaultForDataModelBean(this.ssp, configuration));
            this.objectModels.add(new EnforcedForDataModelBean(this.ssp, configuration));
        }

        return this.objectModels;
    }


    public List<AbstractUsageDataModelBean<? extends StructuralObject>> getServiceModels () throws ModelObjectNotFoundException,
            ModelServiceException, GuiWebServiceException {
        if ( this.serviceModels == null ) {
            this.serviceModels = new LinkedList<>();
            ConfigurationObject configuration = this.getConfiguration();
            this.serviceModels.add(new AffectsServicesDataModelBean(this.ssp, configuration));
            this.serviceModels.add(new DefaultForStructureDataModelBean(this.ssp, configuration));
            this.serviceModels.add(new EnforcedForStructureDataModelBean(this.ssp, configuration));
        }
        return this.serviceModels;
    }


    public String refresh () {
        if ( this.objectModels != null ) {
            for ( AbstractObjectUsageDataModelBean model : this.objectModels ) {
                model.refresh();
            }
        }

        if ( this.serviceModels != null ) {
            for ( AbstractUsageDataModelBean<? extends StructuralObject> model : this.serviceModels ) {
                model.refresh();
            }
        }

        return null;
    }
}
