/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.crypto.tls.TLSContextProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = TLSContextProvider.class )
public class TLSContextProviderImpl implements TLSContextProvider {

    private SecureRandomProvider randomProvider;
    private TLSContextFactory factory;


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randomProvider == srp ) {
            this.randomProvider = null;
        }
    }


    @Reference
    protected synchronized void setTLSContextFactory ( TLSContextFactory tcf ) {
        this.factory = tcf;
    }


    protected synchronized void unsetTLSContextFactory ( TLSContextFactory tcf ) {
        if ( this.factory == tcf ) {
            this.factory = null;
        }
    }


    @Override
    public TLSContext getContext ( InternalTLSConfiguration cfg ) throws CryptoException {
        return new TLSContextImpl(this.factory, cfg, this.randomProvider.getSecureRandom());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContextProvider#update(eu.agno3.runtime.crypto.tls.InternalTLSConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    public void update ( InternalTLSConfiguration cfg, TLSContext ctx ) throws CryptoException {
        if ( ctx instanceof TLSContextImpl ) {
            ( (TLSContextImpl) ctx ).init(this.factory, cfg, this.randomProvider.getSecureRandom());
        }
        else {
            throw new CryptoException("Unexpected context instance " + ctx.getClass().getName()); //$NON-NLS-1$
        }
    }
}
