/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2013 by mbechler
 */
package eu.agno3.runtime.i18n;


import java.util.Locale;
import java.util.ResourceBundle;


/**
 * @author mbechler
 * 
 */
public interface ResourceBundleService {

    /**
     * @return an ResourceBundle.Control instance for accessing OSGI resource bundles
     */
    ResourceBundle.Control getControl ();


    /**
     * 
     * @param baseName
     * @return resource bundle in default locale
     */
    ResourceBundle getBundle ( String baseName );


    /**
     * 
     * @param baseName
     * @param fallbackClassloader
     * @return resource bundle in default locale
     */
    ResourceBundle getBundle ( String baseName, ClassLoader fallbackClassloader );


    /**
     * 
     * @param baseName
     * @param locale
     * @return resource bundle in specified locale
     */
    ResourceBundle getBundle ( String baseName, Locale locale );


    /**
     * @param baseName
     * @param locale
     * @param fallbackClassloader
     * @return resource bundle in specified locale
     */
    ResourceBundle getBundle ( String baseName, Locale locale, ClassLoader fallbackClassloader );

}
