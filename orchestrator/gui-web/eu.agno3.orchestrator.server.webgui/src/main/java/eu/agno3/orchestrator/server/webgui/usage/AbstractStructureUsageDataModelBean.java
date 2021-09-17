/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.util.Comparator;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractStructureUsageDataModelBean extends AbstractUsageDataModelBean<StructuralObject> {

    /**
     * 
     */
    private static final long serialVersionUID = 8641106825990317620L;


    /**
     * @param title
     * @param ssp
     * @param object
     */
    public AbstractStructureUsageDataModelBean ( String title, ServerServiceProvider ssp, ConfigurationObject object ) {
        super(title, ssp, object);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.usage.AbstractUsageDataModelBean#makeDisplayComparator()
     */
    @Override
    protected Comparator<StructuralObject> makeDisplayComparator () {
        return new Comparator<StructuralObject>() {

            @Override
            public int compare ( StructuralObject a, StructuralObject b ) {
                if ( a.getDisplayName() == null && b.getDisplayName() == null ) {
                    return 0;
                }
                else if ( a.getDisplayName() == null ) {
                    return -1;
                }
                else if ( b.getDisplayName() == null ) {
                    return 1;
                }
                return a.getDisplayName().compareTo(b.getDisplayName());
            }

        };
    }

}