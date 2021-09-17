/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.components.listeditor;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
@Named ( "stringSetConverter" )
@ApplicationScoped
public class StringSetConverter implements Converter {

    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String val ) throws ConverterException {

        if ( StringUtils.isBlank(val) ) {
            return Collections.EMPTY_SET;
        }

        String[] items = StringUtils.split(val, ",\n"); //$NON-NLS-1$
        Set<String> res = new HashSet<>();
        for ( String itm : items ) {
            itm = itm.trim();
            if ( StringUtils.isBlank(itm) ) {
                continue;
            }
            res.add(itm);
        }
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object val ) throws ConverterException {
        if ( val == null || ! ( val instanceof Set ) ) {
            return StringUtils.EMPTY;
        }

        return StringUtils.join((Set<?>) val, ", "); //$NON-NLS-1$
    }
}
