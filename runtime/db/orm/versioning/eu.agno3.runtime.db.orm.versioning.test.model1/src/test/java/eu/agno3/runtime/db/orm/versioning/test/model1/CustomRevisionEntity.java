/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.test.model1;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@RevisionEntity ( CustomRevisionListener.class )
public class CustomRevisionEntity extends DefaultRevisionEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -5682473854614323279L;

    @Basic
    private String someExtraString;


    /**
     * @return the someExtraString
     */
    public String getSomeExtraString () {
        return this.someExtraString;
    }


    /**
     * @param someExtraString
     *            the someExtraString to set
     */
    public void setSomeExtraString ( String someExtraString ) {
        this.someExtraString = someExtraString;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.envers.DefaultRevisionEntity#toString()
     */
    @Override
    public String toString () {
        return "CustomRevisionEntity: " + super.toString(); //$NON-NLS-1$
    }
}
