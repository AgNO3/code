/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import jcifs.dcerpc.DcerpcHandle;


/**
 * @author mbechler
 *
 */
public interface NetlogonConnection extends AutoCloseable {

    /**
     * @return the connected realm
     */
    ADRealm getRealm ();


    /**
     * 
     * @return an operations wrapper, do not hold a reference to this
     */
    NetlogonOperations getNetlogonOperations ();


    /**
     * @return the dcerpcHandle
     */
    DcerpcHandle getDcerpcHandle ();


    /**
     * @throws ADException
     * @throws IOException
     */
    void init () throws ADException, IOException;


    /**
     * @return the negotiatedFlags
     */
    int getNegotiatedFlags ();


    /**
     * 
     * @param input
     * @return the data encrypted in the session key
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     */
    byte[] encryptSession ( byte[] input ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, ShortBufferException;


    /**
     * @return a authenticator
     * @throws ADException
     */
    NetlogonAuthenticator authenticate () throws ADException;


    /**
     * 
     * @param returnAuthenticator
     * @param error
     * @throws ADException
     */
    void validate ( NetlogonAuthenticator returnAuthenticator, boolean error ) throws ADException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws ADException;


    /**
     * @return whether the connection is active
     */
    boolean check ();


    /**
     * 
     */
    void fail ();

}