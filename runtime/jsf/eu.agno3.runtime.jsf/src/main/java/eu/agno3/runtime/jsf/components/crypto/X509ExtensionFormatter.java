/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


/**
 * @author mbechler
 *
 */
public interface X509ExtensionFormatter {

    /**
     * 
     * @param data
     *            encoded version of the extension
     * @return a string representation for the extension
     */
    public String format ( byte[] data );
}
