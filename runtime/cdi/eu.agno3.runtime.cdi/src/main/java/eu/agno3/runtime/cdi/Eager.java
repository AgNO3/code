/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2013 by mbechler
 */
package eu.agno3.runtime.cdi;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;


/**
 * @author mbechler
 * 
 */
@Qualifier
@Retention ( RetentionPolicy.RUNTIME )
@Target ( {
    ElementType.TYPE
} )
public @interface Eager {

}
