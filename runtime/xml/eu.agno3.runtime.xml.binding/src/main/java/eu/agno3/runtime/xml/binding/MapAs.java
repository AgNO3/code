/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
public @interface MapAs {

    /**
     * 
     * @return the class whose mapping shall be used for JAXB mapping.
     */
    Class<?> value();
}
