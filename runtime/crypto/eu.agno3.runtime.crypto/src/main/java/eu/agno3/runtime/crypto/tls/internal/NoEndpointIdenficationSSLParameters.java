/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import javax.net.ssl.SSLParameters;


/**
 * @author mbechler
 *
 */
public class NoEndpointIdenficationSSLParameters extends SSLParameters {

    /**
     * 
     */
    public NoEndpointIdenficationSSLParameters () {}


    /**
     * 
     * @param usableCiphers
     */
    public NoEndpointIdenficationSSLParameters ( String[] usableCiphers ) {
        super(usableCiphers);
    }


    /**
     * @param usableCiphers
     * @param usableProtocols
     */
    public NoEndpointIdenficationSSLParameters ( String[] usableCiphers, String[] usableProtocols ) {
        super(usableCiphers, usableProtocols);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.SSLParameters#getEndpointIdentificationAlgorithm()
     */
    @Override
    public String getEndpointIdentificationAlgorithm () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.SSLParameters#setEndpointIdentificationAlgorithm(java.lang.String)
     */
    @Override
    public void setEndpointIdentificationAlgorithm ( String algorithm ) {
        // ignore
    }
}
