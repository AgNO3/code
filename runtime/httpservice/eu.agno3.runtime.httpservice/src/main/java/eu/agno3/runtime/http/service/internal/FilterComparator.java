/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.Serializable;
import java.util.Comparator;

import javax.servlet.Filter;

import eu.agno3.runtime.http.service.filter.FilterConfig;


class FilterComparator implements Comparator<Filter>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8796394706862224407L;


    /**
     * 
     */
    public FilterComparator () {}


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Filter o1, Filter o2 ) {
        FilterConfig f1config = o1.getClass().getAnnotation(FilterConfig.class);
        FilterConfig f2config = o2.getClass().getAnnotation(FilterConfig.class);

        int f1prio = 0;
        int f2prio = 0;

        if ( f1config != null ) {
            f1prio = f1config.priority();
        }

        if ( f2config != null ) {
            f2prio = f2config.priority();
        }

        int res = Integer.compare(f1prio, f2prio);

        if ( res != 0 ) {
            return res;
        }

        return o1.getClass().getName().compareTo(o2.getClass().getName());
    }

}