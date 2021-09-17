/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.runtime.jsf.windowscope;


import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.NormalScope;


/**
 * @author mbechler
 *
 */
@Target ( {
    METHOD, TYPE, FIELD
} )
@Retention ( RUNTIME )
@Inherited
@Documented
@NormalScope ( passivating = true )
public @interface WindowScoped {}
