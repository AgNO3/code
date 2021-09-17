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
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@PersistenceUnit ( unitName = "test" )
@Entity
@SuppressWarnings ( "javadoc" )
public class OneToManySource {

    private long sourceId;
    private String sourceSpec;
    private Set<OneToManyTarget> targets = new HashSet<>();


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


    @OneToMany ( mappedBy = "source", cascade = CascadeType.ALL )
    public Set<OneToManyTarget> getTargets () {
        return this.targets;
    }


    public void setTargets ( Set<OneToManyTarget> targets ) {
        this.targets = targets;
    }

}
