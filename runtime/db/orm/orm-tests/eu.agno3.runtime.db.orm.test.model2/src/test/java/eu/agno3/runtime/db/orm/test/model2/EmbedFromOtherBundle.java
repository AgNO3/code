/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model2;


import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;

import eu.agno3.runtime.db.orm.test.model1.EmbeddableObject;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
public class EmbedFromOtherBundle {

    @Id
    @GeneratedValue
    long id;

    @Embedded
    EmbeddableObject obj;

}
