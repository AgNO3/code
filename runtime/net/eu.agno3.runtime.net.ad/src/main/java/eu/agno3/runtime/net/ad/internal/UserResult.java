/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;

import jcifs.dcerpc.rpc.policy_handle;

final class UserResult {

    private policy_handle handle;
    private int rid;


    /**
     * @param handle
     * @param rid
     * 
     */
    public UserResult ( policy_handle handle, int rid ) {
        this.handle = handle;
        this.rid = rid;
    }


    /**
     * @return the handle
     */
    public policy_handle getHandle () {
        return this.handle;
    }


    /**
     * @return the rid
     */
    public int getRid () {
        return this.rid;
    }
}