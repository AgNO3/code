/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.runtime.mail;


import javax.mail.MessagingException;
import javax.mail.Session;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public interface SMTPTransportFactory {

    /**
     * 
     * @param cfg
     * @param tc
     * @return a unpooled transport
     * @throws MessagingException
     * @throws CryptoException
     */
    public SMTPClientTransport createTransport ( SMTPConfiguration cfg, TLSContext tc ) throws MessagingException, CryptoException;


    /**
     * @param mailConfig
     * @param tlsContext
     * @return a session
     * @throws MessagingException
     * @throws CryptoException
     */
    public Session makeSession ( SMTPConfiguration mailConfig, TLSContext tlsContext ) throws MessagingException, CryptoException;
}
