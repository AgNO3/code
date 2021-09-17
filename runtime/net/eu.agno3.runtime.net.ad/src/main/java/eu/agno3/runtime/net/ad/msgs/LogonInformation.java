/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.ndr.NdrObject;


/**
 * @author mbechler
 *
 */
public abstract class LogonInformation extends NdrObject {

    /**
     * @return NETLOGON_LOGON_INFO_CLASS
     */
    public abstract int getLevel ();

}
