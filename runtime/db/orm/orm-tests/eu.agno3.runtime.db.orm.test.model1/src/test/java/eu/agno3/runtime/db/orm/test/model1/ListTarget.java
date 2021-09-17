/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
public class ListTarget {

    @Id
    @GeneratedValue
    long id;

    public String test;

}
