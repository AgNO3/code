/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.06.2016 by mbechler
 */
package eu.agno3.fileshare.webgui.components;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;


/**
 * Fake converter to work around primefaces #1542
 * 
 * 
 * @author mbechler
 *
 */
@Named ( "fakeStringConverter" )
public class FakeStringConverter implements Converter {

    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String val ) throws ConverterException {
        return val;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext arg0, UIComponent arg1, Object obj ) throws ConverterException {
        if ( obj == null ) {
            return null;
        }
        return obj.toString();
    }

}
