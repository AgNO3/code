/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.types;


import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = TypeContributor.class )
public class FixedLocaleTypeContributor implements TypeContributor {

    /**
     * 
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.model.TypeContributor#contribute(org.hibernate.boot.model.TypeContributions,
     *      org.hibernate.service.ServiceRegistry)
     */
    @Override
    public void contribute ( TypeContributions contribs, ServiceRegistry reg ) {
        contribs.contributeType(FixedLocaleType.INSTANCE);
    }

}
