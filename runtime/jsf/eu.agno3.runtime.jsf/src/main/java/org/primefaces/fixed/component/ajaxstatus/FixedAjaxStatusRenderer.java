/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.09.2016 by mbechler
 */
package org.primefaces.fixed.component.ajaxstatus;


import java.io.IOException;

import javax.faces.context.FacesContext;

import org.primefaces.component.ajaxstatus.AjaxStatus;
import org.primefaces.component.ajaxstatus.AjaxStatusRenderer;
import org.primefaces.util.WidgetBuilder;


/**
 * @author mbechler
 *
 */
public class FixedAjaxStatusRenderer extends AjaxStatusRenderer {

    @Override
    protected void encodeScript ( FacesContext context, AjaxStatus status ) throws IOException {
        String clientId = status.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.initWithDomReady("ExtendedAjaxStatus", status.resolveWidgetVar(), clientId); //$NON-NLS-1$

        wb.callback(AjaxStatus.START, AjaxStatus.CALLBACK_SIGNATURE, status.getOnstart())
                .callback(AjaxStatus.ERROR, AjaxStatus.CALLBACK_SIGNATURE, status.getOnerror())
                .callback(AjaxStatus.SUCCESS, AjaxStatus.CALLBACK_SIGNATURE, status.getOnsuccess())
                .callback(AjaxStatus.COMPLETE, AjaxStatus.CALLBACK_SIGNATURE, status.getOncomplete());

        wb.finish();
    }
}
