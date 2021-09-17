/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 18, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Comparator;

import org.eclipse.jetty.server.Handler;

import eu.agno3.runtime.http.service.handler.ExtendedHandler;


/**
 * @author mbechler
 *
 */
public class HandlerComparator implements Comparator<Handler> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Handler o1, Handler o2 ) {

        if ( ! ( o1 instanceof ExtendedHandler ) && ! ( o2 instanceof ExtendedHandler ) ) {
            return 0;
        }
        else if ( ! ( o1 instanceof ExtendedHandler ) ) {
            return 1;
        }
        else if ( ! ( o2 instanceof ExtendedHandler ) ) {
            return -1;
        }

        ExtendedHandler e1 = (ExtendedHandler) o1;
        ExtendedHandler e2 = (ExtendedHandler) o2;

        return Float.compare(e1.getPriority(), e2.getPriority());
    }

}
