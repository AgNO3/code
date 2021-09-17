/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.validation.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;

class DummyValidationProviderResolver implements ValidationProviderResolver {

    /**
     * 
     */
    public DummyValidationProviderResolver () {}


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidationProviderResolver#getValidationProviders()
     */
    @Override
    public List<ValidationProvider<?>> getValidationProviders () {
        List<ValidationProvider<?>> providers = new ArrayList<>();
        providers.add(new HibernateValidator());
        return providers;
    }
}