/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit;


/**
 * @author mbechler
 *
 */
public class PackageKitException extends Exception {

    private long code;


    /**
     * 
     */
    public PackageKitException () {}


    /**
     * @param code
     * @param details
     */
    public PackageKitException ( long code, String details ) {
        super(details);
        this.code = code;
    }


    /**
     * @param string
     */
    public PackageKitException ( String string ) {
        super(string);
    }


    /**
     * @param string
     * @param e
     */
    public PackageKitException ( String string, Exception e ) {
        super(string, e);
    }


    /**
     * @return the code
     */
    public long getCode () {
        return this.code;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8993928504195936751L;

}
