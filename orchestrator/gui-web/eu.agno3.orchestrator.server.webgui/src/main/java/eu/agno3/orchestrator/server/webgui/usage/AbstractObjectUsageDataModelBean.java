/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.util.Comparator;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractObjectUsageDataModelBean extends AbstractUsageDataModelBean<ConfigurationObject> {

    /**
     * 
     */
    private static final long serialVersionUID = 5542216975892056851L;


    /**
     * @param title
     * @param ssp
     * @param object
     */
    public AbstractObjectUsageDataModelBean ( String title, ServerServiceProvider ssp, ConfigurationObject object ) {
        super(title, ssp, object);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.usage.AbstractUsageDataModelBean#makeDisplayComparator()
     */
    @Override
    protected Comparator<ConfigurationObject> makeDisplayComparator () {
        return new ConfigurationObjectDisplayComparator();

    }
}
