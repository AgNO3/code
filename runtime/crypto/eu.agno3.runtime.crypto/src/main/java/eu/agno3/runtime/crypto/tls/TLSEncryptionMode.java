/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public enum TLSEncryptionMode {

    STREAM,

    CBC,

    GCM,

    NONE;

    public static TLSEncryptionMode fromString ( String val ) {
        return valueOf(val);
    }


    public static String toString ( TLSEncryptionMode algo ) {
        if ( algo == null ) {
            return null;
        }
        return algo.name();
    }
}
