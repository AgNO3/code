/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model2;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;

import eu.agno3.runtime.db.orm.test.model1.ListTarget;


/**
 * @author mbechler
 * 
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
public class ListOf {

    @Id
    @GeneratedValue
    public long listId;

    @OneToMany ( cascade = CascadeType.ALL )
    @OrderColumn ( name = "index" )
    public List<ListTarget> targets = new ArrayList<>();

}
