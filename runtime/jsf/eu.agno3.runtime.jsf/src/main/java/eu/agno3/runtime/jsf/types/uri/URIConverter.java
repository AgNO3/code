/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.types.uri;


import java.net.URI;
import java.net.URISyntaxException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
@Named ( "uriConverter" )
public class URIConverter implements Converter {

    private static final Logger log = Logger.getLogger(URIConverter.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String val ) {

        if ( StringUtils.isBlank(val) ) {
            return null;
        }

        try {
            return new URI(val);
        }
        catch ( URISyntaxException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to parse URI " + val, e); //$NON-NLS-1$
            }
            throw new ConverterException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.format("uriConverter.uriConverterFmt", val), StringUtils.EMPTY), //$NON-NLS-1$
                e);

        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object val ) {

        if ( ! ( val instanceof URI ) ) {
            return null;
        }

        return val.toString();
    }

}
