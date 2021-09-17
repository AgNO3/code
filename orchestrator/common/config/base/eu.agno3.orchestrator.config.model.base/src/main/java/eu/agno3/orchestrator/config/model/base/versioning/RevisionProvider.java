/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.versioning;


import java.util.List;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;


/**
 * @author mbechler
 * 
 */
public interface RevisionProvider {

    /**
     * @return all revisions for the assoicated object
     * @throws AbstractModelException
     */
    List<VersionInfo> fetchAllRevisions () throws AbstractModelException;

}
