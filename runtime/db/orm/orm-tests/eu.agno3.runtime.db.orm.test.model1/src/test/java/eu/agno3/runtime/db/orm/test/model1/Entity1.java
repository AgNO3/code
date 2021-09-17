/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * @author mbechler
 * 
 */
@Entity
@SuppressWarnings ( "javadoc" )
public class Entity1 {

    @Id
    public int eid;

    public String test;
}
