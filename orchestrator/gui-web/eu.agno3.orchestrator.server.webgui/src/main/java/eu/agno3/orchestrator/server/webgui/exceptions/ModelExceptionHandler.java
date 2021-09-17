/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.exceptions;


import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectConflictFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectValidationFault;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;


/**
 * @author mbechler
 * 
 */
public final class ModelExceptionHandler {

    private ModelExceptionHandler () {}


    /**
     * @param msg
     * @param e
     */
    public static void handleException ( String msg, AbstractModelException e ) {
        if ( e instanceof ModelObjectValidationException ) {
            ModelObjectValidationFault fault = ( (ModelObjectValidationException) e ).getFaultInfo();

            if ( fault == null || fault.getEntries() == null || fault.getEntries().isEmpty() ) {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, !StringUtils.isBlank(msg) ? msg : e.getMessage(), e.getMessage()));
            }
            else {
                for ( ViolationEntry violation : fault.getEntries() ) {
                    FacesContext.getCurrentInstance()
                            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, makeViolationMessage(violation)));
                }
            }
        }
        else if ( e instanceof ModelObjectConflictException ) {
            ModelObjectConflictFault cf = ( (ModelObjectConflictException) e ).getFaultInfo();
            ExceptionHandler.addMessage(FacesMessage.SEVERITY_ERROR, "model.conflict", cf.getId(), cf.getObjectType()); //$NON-NLS-1$
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, msg, e.getMessage()));
        }
    }


    private static String makeViolationMessage ( ViolationEntry violation ) {
        return violation.toString();
    }
}
