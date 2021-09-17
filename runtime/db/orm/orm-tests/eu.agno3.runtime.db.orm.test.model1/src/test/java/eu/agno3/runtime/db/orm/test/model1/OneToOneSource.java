/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@PersistenceUnit ( unitName = "test" )
@Entity
@SuppressWarnings ( "javadoc" )
public class OneToOneSource {

    private long sourceId;
    private String sourceSpec;
    private OneToOneTarget target;


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


    @OneToOne
    public OneToOneTarget getTarget () {
        return this.target;
    }


    public void setTarget ( OneToOneTarget target ) {
        this.target = target;
    }

}
