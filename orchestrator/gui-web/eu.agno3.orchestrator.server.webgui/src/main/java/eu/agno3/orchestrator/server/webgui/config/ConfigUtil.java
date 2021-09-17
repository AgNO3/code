/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 * 
 */
@Named ( "configUtil" )
@ApplicationScoped
public class ConfigUtil {

    private static final Logger log = Logger.getLogger(ConfigUtil.class);

    @Inject
    private ConfigLocalizationProvider clp;


    /**
     * 
     * @param typeName
     * @return the edit view template for this object type
     */
    public String getEditViewFor ( String typeName ) {

        if ( typeName == null ) {
            log.warn("Called with NULL"); //$NON-NLS-1$
            return null;
        }

        String prefix = "urn:agno3:objects:1.0:"; //$NON-NLS-1$

        if ( !typeName.startsWith(prefix) ) {
            throw new FacesException("Failed to determine edit view for type " + typeName); //$NON-NLS-1$
        }

        String local = typeName.substring(prefix.length()).replace(':', '/');
        String editView = String.format("/cfg/%s.xhtml", local); //$NON-NLS-1$

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Resolved view %s for type %s", editView, typeName)); //$NON-NLS-1$
        }

        return editView;
    }


    /**
     * 
     * @param o
     * @return whether the given object is anonymous
     */
    public static boolean isAnonymous ( ConfigurationObject o ) {
        return o.getDisplayName() == null && o.getName() == null;
    }


    /**
     * @param obj
     * @return the object type name (given by the type classes ObjectTypeName annotation or from a reference)
     */
    public static String getObjectTypeName ( ConfigurationObject obj ) {
        if ( obj instanceof ConfigurationObjectReference ) {
            return ( (ConfigurationObjectReference) obj ).getObjectTypeName();
        }

        if ( obj == null ) {
            return null;
        }

        ObjectTypeName annot = obj.getType().getAnnotation(ObjectTypeName.class);

        if ( annot == null ) {
            throw new IllegalArgumentException("No ObjectTypeName annotation on " + obj); //$NON-NLS-1$
        }

        return annot.value();
    }


    /**
     * 
     * @param obj
     * @return the display type name for the given object
     */
    public String getDisplayTypeName ( ConfigurationObject obj ) {
        return this.clp.getTypeName(getObjectTypeName(obj));
    }


    /**
     * 
     * @param o
     * @return the proper display name for the given object
     */
    public String getDisplayNameFor ( ConfigurationObject o ) {
        String customDisplayName = getNonLocalizedDisplayNameFor(o);

        if ( customDisplayName != null ) {
            return customDisplayName;
        }

        return GuiMessages.format(GuiMessages.CONFIG_ANONYMOUS_OBJECT, getDisplayTypeName(o));
    }


    /**
     * @param o
     * @return a custom object name
     */
    public static String getNonLocalizedDisplayNameFor ( ConfigurationObject o ) {
        if ( o == null ) {
            return null;
        }

        if ( o.getDisplayName() != null ) {
            return o.getDisplayName();
        }
        else if ( o.getName() != null ) {
            return o.getName();
        }

        return null;
    }


    public static String getObjectName ( ConfigurationObject o ) {
        if ( o == null ) {
            return null;
        }

        @NonNull
        Class<? extends ConfigurationObject> t = o.getType();
        Method[] methods = t.getMethods();
        Method onMethod = null;
        for ( Method m : methods ) {
            if ( m.getParameterCount() != 0 || !String.class.equals(m.getReturnType()) ) {
                continue;
            }
            ObjectName[] on = m.getDeclaredAnnotationsByType(ObjectName.class);
            if ( on != null && on.length > 0 ) {
                onMethod = m;
                break;
            }
        }
        if ( onMethod == null ) {
            return null;
        }

        try {
            return (String) onMethod.invoke(o);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }
}
