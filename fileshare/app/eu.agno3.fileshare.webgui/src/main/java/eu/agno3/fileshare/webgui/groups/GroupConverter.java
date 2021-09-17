/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.groups;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "groupConverter" )
public class GroupConverter implements Converter {

    @Inject
    private FileshareAdminServiceProvider fsp;


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String val ) throws ConverterException {

        if ( StringUtils.isBlank(val) ) {
            return null;
        }

        try {
            return this.fsp.getGroupService().getGroup(UUID.fromString(val));
        }
        catch ( IllegalArgumentException e ) {
            return null;
        }
        catch ( UndeclaredThrowableException e ) {
            if ( e.getCause() instanceof InvocationTargetException && e.getCause() instanceof Exception ) {
                throw new ConverterException(e.getCause().getCause());
            }
            throw e;
        }
        catch ( FileshareException e ) {
            throw new ConverterException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object val ) throws ConverterException {

        if ( ! ( val instanceof Group ) ) {
            return null;
        }

        return ( (Subject) val ).getId().toString();
    }

}
