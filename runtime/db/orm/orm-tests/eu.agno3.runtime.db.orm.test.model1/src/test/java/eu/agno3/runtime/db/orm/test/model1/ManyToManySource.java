/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@PersistenceUnit ( unitName = "test" )
@Entity
@SuppressWarnings ( "javadoc" )
public class ManyToManySource {

    private long sourceId;
    private String sourceSpec;
    private Set<ManyToManyTarget> targets = new HashSet<>();


    @Id
    @GeneratedValue
    public long getSourceId () {
        return this.sourceId;
    }


    public void setSourceId ( long sourceId ) {
        this.sourceId = sourceId;
    }


    @Basic
    public String getSourceSpec () {
        return this.sourceSpec;
    }


    public void setSourceSpec ( String sourceSpec ) {
        this.sourceSpec = sourceSpec;
    }


    @ManyToMany ( cascade = CascadeType.ALL )
    public Set<ManyToManyTarget> getTargets () {
        return this.targets;
    }


    public void setTargets ( Set<ManyToManyTarget> targets ) {
        this.targets = targets;
    }

}
