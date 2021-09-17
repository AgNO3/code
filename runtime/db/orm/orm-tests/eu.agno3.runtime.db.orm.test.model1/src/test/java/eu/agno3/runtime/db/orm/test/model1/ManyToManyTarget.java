/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
@PersistenceUnit ( unitName = "test" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
abstract public class ManyToManyTarget {

    private long targetId;
    private Set<ManyToManySource> sources = new HashSet<>();


    @Id
    @GeneratedValue
    public long getTargetId () {
        return this.targetId;
    }


    public void setTargetId ( long targetId ) {
        this.targetId = targetId;
    }


    @ManyToMany ( mappedBy = "targets" )
    public Set<ManyToManySource> getSources () {
        return this.sources;
    }


    public void setSources ( Set<ManyToManySource> sources ) {
        this.sources = sources;
    }

}
