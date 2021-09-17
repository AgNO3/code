/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.model.query.SubjectQueryResult;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_groupQueryResultConverter" )
public class GroupQueryResultConverter implements Converter {

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

        return SubjectQueryResult.fromString(val);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object val ) throws ConverterException {

        if ( ! ( val instanceof GroupQueryResult ) ) {
            return null;
        }

        return ( (GroupQueryResult) val ).toString();
    }

}
