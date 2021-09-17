/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.ndr.NdrObject;


/**
 * @author mbechler
 *
 */
public abstract class DomainInformation extends NdrObject {

    /**
     * @return th
     */
    public abstract int getDomainInformationClass ();

}
