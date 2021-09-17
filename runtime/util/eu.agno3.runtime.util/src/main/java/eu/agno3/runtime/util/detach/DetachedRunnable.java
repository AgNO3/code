/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2015 by mbechler
 */
package eu.agno3.runtime.util.detach;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface DetachedRunnable <T> {

    /**
     * @return return value
     * @throws Exception
     */
    T run () throws Exception;

}
