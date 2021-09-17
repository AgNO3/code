/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 23, 2016 by mbechler
 */
package org.primefaces.fixed.component.wizard;


import java.io.IOException;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.tabview.Tab;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.component.wizard.WizardRenderer;
import org.primefaces.util.ComponentTraversalUtils;


/**
 * @author mbechler
 *
 */
public class WizardRendererFixed extends WizardRenderer {

    @Override
    @SuppressWarnings ( {
        "nls", "resource"
    } )
    protected void encodeScript ( FacesContext context, Wizard wizard ) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = wizard.getClientId(context);

        UIComponent form = ComponentTraversalUtils.closestForm(context, wizard);
        if ( form == null ) {
            throw new FacesException("Wizard : \"" + clientId + "\" must be inside a form element");
        }

        startScript(writer, clientId);

        writer.write("$(function() {");

        writer.write("PrimeFaces.cw('WizardFixed','" + wizard.resolveWidgetVar() + "',{");
        writer.write("id:'" + clientId + "'");
        writer.write(",showStepStatus:" + wizard.isShowStepStatus());
        writer.write(",showNavBar:" + wizard.isShowNavBar());

        if ( wizard.getOnback() != null ) {
            writer.write(",onback:function(){" + wizard.getOnback() + "}");
        }
        if ( wizard.getOnnext() != null ) {
            writer.write(",onnext:function(){" + wizard.getOnnext() + "}");
        }

        if ( wizard instanceof WizardFixed ) {
            WizardFixed wf = (WizardFixed) wizard;

            if ( wf.getOnstart() != null ) {
                writer.write(",onstart:function(){" + wf.getOnstart() + "}");
            }
            if ( wf.getOncomplete() != null ) {
                writer.write(",oncomplete:function(){" + wf.getOncomplete() + "}");
            }
            if ( wf.getOnerror() != null ) {
                writer.write(",onerror:function(){" + wf.getOnerror() + "}");
            }
            if ( wf.getOnsuccess() != null ) {
                writer.write(",onerror:function(){" + wf.getOnsuccess() + "}");
            }
            if ( wf.getProcess() != null ) {
                writer.write(",process: '" + wf.getProcess() + "'");
            }
            if ( wf.getUpdate() != null ) {
                writer.write(",update: '" + wf.getUpdate() + "'");
            }
        }

        // all steps
        writer.write(",steps:[");
        boolean firstStep = true;
        String defaultStep = null;
        for ( Iterator<UIComponent> children = wizard.getChildren().iterator(); children.hasNext(); ) {
            UIComponent child = children.next();

            if ( child instanceof Tab && child.isRendered() ) {
                Tab tab = (Tab) child;
                if ( defaultStep == null ) {
                    defaultStep = tab.getId();
                }

                if ( !firstStep ) {
                    writer.write(",");
                }
                else {
                    firstStep = false;
                }

                writer.write("'" + tab.getId() + "'");
            }
        }
        writer.write("]");

        // current step
        if ( wizard.getStep() == null ) {
            wizard.setStep(defaultStep);
        }

        writer.write(",initialStep:'" + wizard.getStep() + "'");

        writer.write("});});");

        endScript(writer);
    }
}
