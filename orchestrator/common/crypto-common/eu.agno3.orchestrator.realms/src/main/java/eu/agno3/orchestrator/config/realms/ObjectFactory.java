/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return a object instance
     */
    public KerberosConfig createKerberosConfig () {
        return new KerberosConfigImpl();
    }


    /**
     * 
     * @return a object instance
     */
    public RealmsConfig createRealmsConfig () {
        return new RealmsConfigImpl();
    }


    /**
     * @return a object instance
     */
    public ADRealmConfig createADRealmConfig () {
        return new ADRealmConfigImpl();
    }


    /**
     * 
     * @return a object instance
     */
    public KRBRealmConfig createKRBRealmConfig () {
        return new KRBRealmConfigImpl();
    }


    /**
     * @return a object instance
     */
    public CAPathEntry createCAPathEntry () {
        return new CAPathEntryImpl();
    }


    /**
     * 
     * @return a object instance
     */
    public KeytabEntry createKeytabEntry () {
        return new KeytabEntryImpl();
    }
}
