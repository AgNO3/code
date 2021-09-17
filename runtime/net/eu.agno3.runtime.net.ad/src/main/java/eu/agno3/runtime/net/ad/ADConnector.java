/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.io.IOException;

import jcifs.CIFSContext;
import jcifs.dcerpc.DcerpcHandle;


/**
 * @author mbechler
 *
 */
public interface ADConnector {

    /**
     * 
     * @param realm
     * @param ctx
     * @param pipeName
     * @param unshared
     * @return a dcerpc endpoint
     * @throws ADException
     */
    DcerpcHandle getEndpoint ( ADRealm realm, CIFSContext ctx, String pipeName, boolean unshared ) throws ADException;


    /**
     * 
     * @param realm
     * @param ctx
     * @param subject
     * @return a NETLOGON dcerpc endpoint
     * @throws ADException
     */
    DcerpcHandle getNetlogonEndpoint ( ADRealm realm, CIFSContext ctx ) throws ADException;


    /**
     * 
     * @param realm
     * @param ctx
     * @param subject
     * @return a SAMR dcerpc endpoint
     * @throws ADException
     */
    DcerpcHandle getSAMREndpoint ( ADRealm realm, CIFSContext ctx ) throws ADException;


    /**
     * 
     * @param realm
     * @param ctx
     * @param username
     * @param password
     * @return a SAMR dcerpc endpoint
     * @throws ADException
     */
    DcerpcHandle getSAMREndpointWithPassword ( ADRealm realm, String username, String password ) throws ADException;


    /**
     * 
     * @param realm
     * @param subj
     * @return a authenticated netlogon connection
     * @throws ADException
     * @throws IOException
     */
    NetlogonConnection getNetlogonConnection ( ADRealm realm, CIFSContext subj ) throws ADException, IOException;

}