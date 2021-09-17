/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
public class MapSource {

    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    public long id;

    @OneToMany ( mappedBy = "source", cascade = CascadeType.ALL )
    @MapKey ( name = "keyValue" )
    public Map<String, MapTarget> targetsImplicit = new HashMap<>();

    @OneToMany ( cascade = CascadeType.ALL )
    @JoinTable ( name = "explicit_targets", joinColumns = @JoinColumn ( referencedColumnName = "id" ) )
    @MapKeyColumn ( name = "mk", unique = true )
    public Map<String, MapTarget> targetsExplicit = new HashMap<>();

    @OneToMany ( cascade = CascadeType.ALL )
    @JoinTable ( name = "ternary_targets", joinColumns = @JoinColumn ( referencedColumnName = "id" ) )
    @MapKeyJoinColumn ( name = "entityid" )
    public Map<Entity2, MapTarget> targetsTernary = new HashMap<>();

}
