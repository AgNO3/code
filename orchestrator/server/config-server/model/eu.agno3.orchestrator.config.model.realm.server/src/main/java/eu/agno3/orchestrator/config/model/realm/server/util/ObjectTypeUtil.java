/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * 
 */
public final class ObjectTypeUtil {

    /**
     * 
     */
    private ObjectTypeUtil () {}


    /**
     * Determine proper object type
     * 
     * Walks up inheritance hierarchy and look for ObjectTypeName annotation.
     * 
     * @param objType
     * @return the object type class
     */
    @SuppressWarnings ( "unchecked" )
    public static Class<? extends ConfigurationObject> findObjectType ( Class<?> objType ) {

        ObjectTypeName nameAnnot = objType.getAnnotation(ObjectTypeName.class);
        if ( nameAnnot != null ) {
            return (Class<? extends ConfigurationObject>) objType;
        }

        for ( Class<?> intf : objType.getInterfaces() ) {
            nameAnnot = intf.getAnnotation(ObjectTypeName.class);
            if ( nameAnnot != null ) {
                return (Class<? extends ConfigurationObject>) intf;
            }

            Class<? extends ConfigurationObject> inherited = findObjectType(intf);
            if ( inherited != null ) {
                return inherited;
            }
        }

        return null;
    }


    /**
     * @param obj
     * @return the object type name
     */
    public static String getObjectType ( ConfigurationObject obj ) {
        Class<? extends ConfigurationObject> objClass = ObjectTypeUtil.findObjectType(obj.getType());
        ObjectTypeName otn = objClass.getAnnotation(ObjectTypeName.class);
        return otn.value();
    }

}
