/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 24, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public final class PathUtil {

    private static final Logger log = Logger.getLogger(PathUtil.class);


    /**
     * 
     */
    private PathUtil () {}


    /**
     * 
     * @param o
     * @param path
     * @return the child editor for the specified relative path
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    public static ConfigurationObject resolvePath ( ConfigurationObject o, String path ) throws ModelObjectNotFoundException, ModelServiceException {

        String[] fragments = StringUtils.split(path, '/');

        if ( log.isDebugEnabled() ) {
            log.debug("Resolving " + fragments); //$NON-NLS-1$
            log.debug("Object is " + o); //$NON-NLS-1$
        }

        ConfigurationObject cur = o;

        Iterator<String> fragmentIt = new ArrayIterator<>(fragments);

        while ( fragmentIt.hasNext() ) {
            String frag = fragmentIt.next();

            if ( frag.startsWith("obj:") ) { //$NON-NLS-1$
                UUID expectId = UUID.fromString(frag.substring(4));
                if ( !expectId.equals(cur.getId()) ) {
                    throw new ModelObjectNotFoundException(cur.getType(), expectId);
                }
                continue;
            }
            else if ( frag.startsWith("col:") ) { //$NON-NLS-1$
                throw new ModelServiceException("Found unexpected collection reference"); //$NON-NLS-1$
            }

            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(cur.getType());
            }
            catch ( IntrospectionException e ) {
                throw new ModelServiceException("Failed to introspect object", e); //$NON-NLS-1$
            }

            PropertyDescriptor found = null;
            for ( PropertyDescriptor pd : beanInfo.getPropertyDescriptors() ) {
                if ( frag.equals(pd.getName()) ) {
                    found = pd;
                    break;
                }
            }

            if ( found == null || found.getReadMethod() == null ) {
                throw new ModelServiceException("Non existant or inaccessible property in path " + frag); //$NON-NLS-1$
            }

            ReferencedObject refObj = ReflectionUtil.getReference(cur.getType(), found.getReadMethod());

            if ( refObj == null ) {
                throw new ModelServiceException("Property not an object reference " + frag); //$NON-NLS-1$
            }

            Object val;
            try {
                val = found.getReadMethod().invoke(cur);
            }
            catch (
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e ) {
                throw new ModelServiceException("Failed to read property " + frag); //$NON-NLS-1$
            }

            if ( val instanceof Collection ) {
                if ( !fragmentIt.hasNext() ) {
                    throw new ModelServiceException("Path is missing collection element"); //$NON-NLS-1$
                }
                String elemFrag = fragmentIt.next();
                if ( !elemFrag.startsWith("col:") ) { //$NON-NLS-1$
                    throw new ModelServiceException("Collection is missing object reference"); //$NON-NLS-1$
                }

                UUID colId = UUID.fromString(elemFrag.substring(4));

                Collection<?> c = (Collection<?>) val;
                ConfigurationObject foundInCollection = null;
                for ( Object co : c ) {
                    if ( co instanceof ConfigurationObject && ( (ConfigurationObject) co ).getId().equals(colId) ) {
                        foundInCollection = (ConfigurationObject) co;
                        break;
                    }
                }

                if ( foundInCollection == null ) {
                    throw new ModelObjectNotFoundException(ConfigurationObject.class, colId);
                }

                cur = foundInCollection;
            }
            else if ( val instanceof ConfigurationObject ) {
                cur = (ConfigurationObject) val;
            }
            else if ( val == null ) {
                throw new ModelServiceException("Reference is null " + frag); //$NON-NLS-1$
            }
            else {
                throw new ModelServiceException("Unexpected object " + val); //$NON-NLS-1$
            }
        }

        return cur;
    }

}
