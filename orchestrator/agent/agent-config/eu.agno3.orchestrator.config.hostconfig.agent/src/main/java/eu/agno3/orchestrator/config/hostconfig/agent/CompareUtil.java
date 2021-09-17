/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public final class CompareUtil {

    private static final Logger log = Logger.getLogger(CompareUtil.class);


    /**
     * 
     */
    private CompareUtil () {}


    /**
     * @param a
     * @param b
     * @return whether the two configuration object are equal
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static boolean compareProperties ( ConfigurationObject a, ConfigurationObject b )
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {

        if ( a == null && b == null ) {
            return true;
        }
        else if ( a == null ) {
            return false;
        }
        else if ( b == null ) {
            return false;
        }

        if ( !Objects.equals(a.getType(), b.getType()) ) {
            return false;
        }

        return comparePropertiesInternal(a, b);
    }


    /**
     * @param a
     * @param b
     * @return
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static boolean comparePropertiesInternal ( ConfigurationObject a, ConfigurationObject b )
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(a.getType());

        for ( PropertyDescriptor desc : beanInfo.getPropertyDescriptors() ) {
            if ( !compareProperty(a, b, desc) ) {
                return false;
            }
        }

        return true;
    }


    /**
     * @param a
     * @param b
     * @param desc
     * @return
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static boolean compareProperty ( ConfigurationObject a, ConfigurationObject b, PropertyDescriptor desc )
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        if ( ConfigurationObject.class.isAssignableFrom(desc.getPropertyType()) ) {
            throw new IntrospectionException("Not a value type " + desc.getName()); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Comparing property " + desc.getName()); //$NON-NLS-1$
        }

        Method read = desc.getReadMethod();
        Object aVal = read.invoke(a);
        Object bVal = read.invoke(b);
        return compareValues(desc, aVal, bVal);
    }


    /**
     * @param desc
     * @param aVal
     * @param bVal
     * @return
     */
    private static boolean compareValues ( PropertyDescriptor desc, Object aVal, Object bVal ) {
        if ( !Objects.equals(aVal, bVal) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Mismatch in property " + desc.getName()); //$NON-NLS-1$
                if ( log.isTraceEnabled() ) {
                    log.trace("A: " + aVal); //$NON-NLS-1$
                    log.trace("B: " + bVal); //$NON-NLS-1$
                }
            }
            return false;
        }

        return true;
    }
}
