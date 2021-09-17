/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


/**
 * @author mbechler
 * 
 */
public interface PackageToNamespaceResolver {

    /**
     * @param packageName
     * @return the namespace for this package
     */
    String getNamespaceForPackage ( Package packageName );
}
