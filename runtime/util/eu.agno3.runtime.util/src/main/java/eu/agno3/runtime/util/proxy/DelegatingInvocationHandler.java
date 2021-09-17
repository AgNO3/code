/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2013 by mbechler
 */
package eu.agno3.runtime.util.proxy;


import java.lang.reflect.InvocationHandler;


/**
 * @author mbechler
 * 
 */
public interface DelegatingInvocationHandler extends InvocationHandler {

    /**
     * @return the object to which calls are delegated
     */
    Object getDelegate ();

}
