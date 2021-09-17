/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


/**
 * @author mbechler
 *
 */
public interface ICAPConnectionPool {

    /**
     * 
     * @return a pooled connection
     * @throws ICAPException
     */
    ICAPConnection getConnection () throws ICAPException;


    /**
     * @return a fresh non-pooled connection
     * @throws ICAPException
     */
    ICAPConnection createConnection () throws ICAPException;


    /**
     * @param req
     * @throws ICAPException
     * @throws ICAPScannerException
     */
    void scan ( ICAPScanRequest req ) throws ICAPException, ICAPScannerException;

}