/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Comparator;

import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 *
 */
public class ObjectTypeComparator implements Comparator<ObjectTypeDescriptor<?>> {

    private static final String PREFIX = "urn:agno3:objects:1.0:"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( ObjectTypeDescriptor<?> o1, ObjectTypeDescriptor<?> o2 ) {
        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        boolean o1base = isBaseObject(o1);
        boolean o2base = isBaseObject(o2);
        if ( o1base && o2base ) {
            // falltrough
        }
        else if ( o1base ) {
            return 1;
        }
        else if ( o2base ) {
            return -1;
        }
        return o1.getObjectTypeName().compareTo(o2.getObjectTypeName());
    }


    /**
     * @param o2
     * @return
     */
    private static boolean isBaseObject ( ObjectTypeDescriptor<?> t ) {
        if ( t.getObjectTypeName().length() <= PREFIX.length() ) {
            return false;
        }

        String local = t.getObjectTypeName().substring(PREFIX.length());
        if ( local.startsWith("web:") || //$NON-NLS-1$
                local.startsWith("auth:") ) { //$NON-NLS-1$
            return true;
        }
        return false;
    }
}
