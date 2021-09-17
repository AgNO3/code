/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 25, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
public class Joda {

    @Id
    @GeneratedValue ( strategy = GenerationType.AUTO )
    public int id;

    public DateTime value;
}
