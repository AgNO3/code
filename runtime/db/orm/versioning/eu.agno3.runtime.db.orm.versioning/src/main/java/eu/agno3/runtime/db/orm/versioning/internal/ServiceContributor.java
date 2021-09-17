/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.10.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.internal;


import org.hibernate.envers.boot.internal.EnversServiceContributor;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    org.hibernate.service.spi.ServiceContributor.class, EnversServiceContributor.class
} )
public class ServiceContributor extends EnversServiceContributor {

}
