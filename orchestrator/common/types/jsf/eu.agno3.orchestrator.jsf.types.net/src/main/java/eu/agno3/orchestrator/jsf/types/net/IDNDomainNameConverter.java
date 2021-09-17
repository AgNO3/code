/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import java.net.IDN;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;


/**
 * @author mbechler
 * 
 */
@Named ( "dnsIdnConverter" )
@FacesConverter
public class IDNDomainNameConverter implements Converter {

    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public String getAsObject ( FacesContext ctx, UIComponent comp, String val ) {
        if ( val == null ) {
            return null;
        }

        if ( isPureASCII(val) ) {
            return val;
        }

        try {
            return IDN.toASCII(val);
        }
        catch ( IllegalArgumentException e ) {
            throw new ConverterException(NetTypesMessages.get(NetTypesMessages.IDN_INVALID), e);
        }
    }


    private static boolean isPureASCII ( String toCheck ) {
        for ( int i = 0; i < toCheck.length(); i++ ) {
            if ( toCheck.charAt(i) > 0x7F ) {
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object val ) {
        if ( ! ( val instanceof String ) ) {
            return null;
        }
        return IDN.toUnicode((String) val);
    }

}
