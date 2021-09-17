/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
@PersistenceUnit ( unitName = "test" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
abstract public class OneToManyTarget {

    private long targetId;
    private OneToManySource source;


    @Id
    @GeneratedValue
    public long getTargetId () {
        return this.targetId;
    }


    public void setTargetId ( long targetId ) {
        this.targetId = targetId;
    }


    @ManyToOne
    public OneToManySource getSource () {
        return this.source;
    }


    public void setSource ( OneToManySource source ) {
        this.source = source;
    }

}
