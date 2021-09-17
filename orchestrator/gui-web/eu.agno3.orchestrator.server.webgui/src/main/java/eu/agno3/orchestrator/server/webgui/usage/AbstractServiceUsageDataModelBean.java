/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.util.Comparator;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractServiceUsageDataModelBean extends AbstractUsageDataModelBean<ServiceStructuralObject> {

    /**
     * 
     */
    private static final long serialVersionUID = 8641106825990317620L;


    /**
     * @param title
     * @param ssp
     * @param object
     */
    public AbstractServiceUsageDataModelBean ( String title, ServerServiceProvider ssp, ConfigurationObject object ) {
        super(title, ssp, object);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.usage.AbstractUsageDataModelBean#makeDisplayComparator()
     */
    @Override
    protected Comparator<ServiceStructuralObject> makeDisplayComparator () {
        return new Comparator<ServiceStructuralObject>() {

            @Override
            public int compare ( ServiceStructuralObject a, ServiceStructuralObject b ) {
                int res = a.getServiceType().compareTo(b.getServiceType());

                if ( res != 0 ) {
                    return res;
                }

                return a.getDisplayName().compareTo(b.getDisplayName());
            }

        };
    }

}