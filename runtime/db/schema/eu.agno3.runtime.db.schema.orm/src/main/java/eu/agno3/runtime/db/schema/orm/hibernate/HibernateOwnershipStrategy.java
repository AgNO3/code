/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


import org.hibernate.mapping.Table;


/**
 * Map database objects to their owning bundles
 * 
 * The respective owner is repsonsible for providing appropriate schema contributions.
 * 
 * @author mbechler
 * 
 */

public interface HibernateOwnershipStrategy {

    /**
     * 
     * @param t
     * @return the table owner
     */
    String getOwner ( Table t );
}
