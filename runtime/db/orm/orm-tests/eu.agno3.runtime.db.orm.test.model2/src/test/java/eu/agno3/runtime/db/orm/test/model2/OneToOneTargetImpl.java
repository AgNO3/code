/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model2;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;

import eu.agno3.runtime.db.orm.test.model1.OneToOneTarget;


/**
 * @author mbechler
 * 
 */
@PersistenceUnit ( unitName = "test" )
@Entity
@SuppressWarnings ( "javadoc" )
public class OneToOneTargetImpl extends OneToOneTarget {

    private String targetSpec;
    private String foo;


    @Basic
    public String getTargetSpec () {
        return this.targetSpec;
    }


    public void setTargetSpec ( String targetSpec ) {
        this.targetSpec = targetSpec;
    }


    @Basic
    public String getFoo () {
        return this.foo;
    }


    /**
     * @param foo
     *            the foo to set
     */
    public void setFoo ( String foo ) {
        this.foo = foo;
    }
}
