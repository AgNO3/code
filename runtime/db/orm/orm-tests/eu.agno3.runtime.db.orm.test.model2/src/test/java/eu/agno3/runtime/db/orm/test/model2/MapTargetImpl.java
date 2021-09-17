/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model2;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;

import eu.agno3.runtime.db.orm.test.model1.MapTarget;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
public class MapTargetImpl extends MapTarget {

    @Basic
    public String targetName;
}
