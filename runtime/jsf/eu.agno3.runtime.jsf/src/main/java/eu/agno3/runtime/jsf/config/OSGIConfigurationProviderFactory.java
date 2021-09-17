/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.05.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.faces.context.ExternalContext;

import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.FacesConfigurationProviderFactory;


/**
 * @author mbechler
 * 
 */
public class OSGIConfigurationProviderFactory extends FacesConfigurationProviderFactory {

    private FacesConfigurationProviderFactory delegate;


    /**
     * @param delegate
     */
    public OSGIConfigurationProviderFactory ( FacesConfigurationProviderFactory delegate ) {
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.spi.FacesConfigurationProviderFactory#getFacesConfigurationProvider(javax.faces.context.ExternalContext)
     */
    @Override
    public FacesConfigurationProvider getFacesConfigurationProvider ( ExternalContext ctx ) {
        return new OSGIConfigurationProvider(this.delegate.getFacesConfigurationProvider(ctx));
    }

}
