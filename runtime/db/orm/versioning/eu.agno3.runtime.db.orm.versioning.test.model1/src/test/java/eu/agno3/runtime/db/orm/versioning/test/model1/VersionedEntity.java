/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.test.model1;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Version;

import org.hibernate.envers.Audited;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@Audited
@SuppressWarnings ( "javadoc" )
public class VersionedEntity {

    @Id
    @GeneratedValue
    public long id;

    @Version
    long serial;

    @Basic
    public String someValString;

    @Basic
    @Column ( nullable = true )
    public int someValInt;

}
