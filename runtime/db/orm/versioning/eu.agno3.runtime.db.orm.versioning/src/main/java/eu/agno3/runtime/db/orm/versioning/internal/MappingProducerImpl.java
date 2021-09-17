/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.internal;


import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.envers.boot.internal.AdditionalJaxbMappingProducerImpl;
import org.hibernate.envers.boot.internal.EnversServiceContributor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "deprecation" )
@Component ( service = {
    AdditionalJaxbMappingProducer.class, AdditionalJaxbMappingProducerImpl.class
} )
public class MappingProducerImpl extends AdditionalJaxbMappingProducerImpl {

    @Reference
    protected synchronized void setEnversService ( EnversServiceContributor e ) {
        // dep only
    }


    protected synchronized void unsetEnversService ( EnversServiceContributor e ) {
        // dep only
    }
}
