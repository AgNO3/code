/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


import org.hibernate.mapping.Table;


/**
 * @author mbechler
 * 
 */
public class NOPHibernateOwnershipStrategy implements HibernateOwnershipStrategy {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategy#getOwner(org.hibernate.mapping.Table)
     */
    @Override
    public String getOwner ( Table t ) {
        return null;
    }

}
