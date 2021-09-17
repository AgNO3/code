/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package org.primefaces.fixed.component.toolbar;


import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.toolbar.Toolbar;
import org.primefaces.component.toolbar.ToolbarRenderer;


/**
 * @author mbechler
 *
 */
public class ExtendedToolbarRenderer extends ToolbarRenderer {

    @Override
    @SuppressWarnings ( "nls" )
    public void encodeEnd ( FacesContext context, UIComponent component ) throws IOException {
        Toolbar toolbar = (Toolbar) component;
        if ( ! ( toolbar instanceof ExtendedToolbar ) ) {
            super.encodeEnd(context, component);
            return;
        }

        ExtendedToolbar et = (ExtendedToolbar) toolbar;

        @SuppressWarnings ( "resource" )
        ResponseWriter writer = context.getResponseWriter();
        String style = toolbar.getStyle();
        String styleClass = toolbar.getStyleClass();
        styleClass = styleClass == null ? Toolbar.CONTAINER_CLASS : Toolbar.CONTAINER_CLASS + " " + styleClass;

        writer.startElement("div", toolbar);
        writer.writeAttribute("id", toolbar.getClientId(context), null);
        writer.writeAttribute("class", styleClass, null);
        writer.writeAttribute("role", "toolbar", null);
        if ( style != null ) {
            writer.writeAttribute("style", style, null);
        }

        if ( et.getReverseBreak() ) {
            encodeFacet(context, et, "right", et.getRightStyle());
            encodeFacet(context, et, "left", et.getLeft());
        }
        else {
            encodeFacet(context, et, "left", et.getLeft());
            encodeFacet(context, et, "right", et.getRightStyle());
        }

        writer.endElement("div");
    }


    @SuppressWarnings ( "nls" )
    protected void encodeFacet ( FacesContext context, ExtendedToolbar toolbar, String facetName, String style ) throws IOException {
        @SuppressWarnings ( "resource" )
        ResponseWriter writer = context.getResponseWriter();
        UIComponent facet = toolbar.getFacet(facetName);

        if ( facet != null ) {
            writer.startElement("div", null);
            writer.writeAttribute("class", "ui-toolbar-group-" + facetName, null);

            if ( !StringUtils.isBlank(style) ) {
                writer.writeAttribute("style", style, null);
            }

            facet.encodeAll(context);
            writer.endElement("div");
        }
    }

}
