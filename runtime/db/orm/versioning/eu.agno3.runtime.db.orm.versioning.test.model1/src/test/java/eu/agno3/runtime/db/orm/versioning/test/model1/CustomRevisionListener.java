/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.test.model1;


import org.hibernate.envers.RevisionListener;


/**
 * @author mbechler
 * 
 */
public class CustomRevisionListener implements RevisionListener {

    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.envers.RevisionListener#newRevision(java.lang.Object)
     */
    @Override
    public void newRevision ( Object revInfo ) {
        CustomRevisionEntity rev = (CustomRevisionEntity) revInfo;
        rev.setSomeExtraString("foo"); //$NON-NLS-1$
    }

}
