/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface GroupServiceInternal extends GroupServiceMBean {

    /**
     * @param tx
     * @param group
     */
    void deleteGroup ( EntityTransactionContext tx, Group group );

}
