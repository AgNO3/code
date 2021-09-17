/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.internal;


import org.hibernate.envers.boot.internal.TypeContributorImpl;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    org.hibernate.boot.model.TypeContributor.class, TypeContributor.class
} )
public class TypeContributor extends TypeContributorImpl {

}
