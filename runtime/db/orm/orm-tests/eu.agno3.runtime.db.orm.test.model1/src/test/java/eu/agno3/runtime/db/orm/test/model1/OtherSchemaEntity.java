/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


/**
 * @author mbechler
 * 
 */
@PersistenceUnit ( unitName = "test" )
@Entity
@Table ( schema = "FOO" )
public class OtherSchemaEntity {

    @Id
    @GeneratedValue
    private int id;

    @Basic ( optional = true )
    private String foo;


    /**
     * @return the id
     */
    public int getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( int id ) {
        this.id = id;
    }


    /**
     * @return the foo
     */
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
