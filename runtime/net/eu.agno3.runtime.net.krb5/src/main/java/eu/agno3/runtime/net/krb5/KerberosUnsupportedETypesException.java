/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 17, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5;


/**
 * @author mbechler
 *
 */
public class KerberosUnsupportedETypesException extends KerberosException {

    /**
     * 
     */
    private static final long serialVersionUID = -7504142551709781582L;
    private String[] allowedETypes;


    /**
     * 
     */
    public KerberosUnsupportedETypesException () {}


    /**
     * @param m
     * @param allowedETypes
     */
    public KerberosUnsupportedETypesException ( String m, String[] allowedETypes ) {
        super(m);
        this.allowedETypes = allowedETypes;
    }


    /**
     * @param allowedETypes
     * @param t
     */
    public KerberosUnsupportedETypesException ( String[] allowedETypes, Throwable t ) {
        super(t);
        this.allowedETypes = allowedETypes;
    }


    /**
     * @param m
     * @param allowedETypes
     * @param t
     */
    public KerberosUnsupportedETypesException ( String m, String[] allowedETypes, Throwable t ) {
        super(m, t);
        this.allowedETypes = allowedETypes;
    }


    /**
     * @return the allowedETypes
     */
    public String[] getRequestedETypes () {
        return this.allowedETypes;
    }

}
