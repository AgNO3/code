/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.db.versioning;


import org.hibernate.envers.RevisionListener;


/**
 * @author mbechler
 * 
 */
public class RevisionEntityListener implements RevisionListener {

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.envers.RevisionListener#newRevision(java.lang.Object)
     */
    @Override
    public void newRevision ( Object revisionEntity ) {

    }

}
