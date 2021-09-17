/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Formula;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
@Table ( indexes = {
    @Index ( name = "multiIndex", columnList = "multiplicator,multiplied" )
} )
public class BasicTypes {

    @Id
    @Column ( updatable = false, nullable = false, length = 16 )
    UUID id;

    @Version
    long version;

    @Temporal ( TemporalType.DATE )
    Calendar date;

    @Temporal ( TemporalType.TIME )
    Date time;

    @Temporal ( TemporalType.TIMESTAMP )
    Calendar timestamp;

    @Lob
    String clob;

    @Lob
    byte[] blob;

    @Transient
    int unmapped;

    @Column ( unique = true, nullable = false, name = "renamedUnique" )
    String unique;

    @Basic
    int multiplicator;

    @Basic
    int multiplied;

    @Formula ( "multiplicator * multiplied" )
    int result;

    @Enumerated ( EnumType.STRING )
    TestEnum enumTest;

    @Embedded
    EmbeddableObject embedTest;

    @Embedded
    @AttributeOverrides ( {
        @AttributeOverride ( name = "valA", column = @Column ( name = "xValA" ) ),
        @AttributeOverride ( name = "valB", column = @Column ( name = "xValB" ) )
    } )
    EmbeddableObject embedTestB;

    public enum TestEnum {
        UNSPECIFIED, FIRSTVAL, SECONDVAL
    }

    @ElementCollection
    Set<String> collTest;

    @ElementCollection
    Set<EmbeddableObject> embedCollTest;

}
