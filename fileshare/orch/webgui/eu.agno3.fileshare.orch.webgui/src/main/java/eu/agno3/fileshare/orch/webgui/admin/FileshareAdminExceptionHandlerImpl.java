/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.orch.webgui.FileshareOrchGUIMessages;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareAdminExceptionHandlerImpl implements FileshareAdminExceptionHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 8611842304348429521L;

    private static final Logger log = Logger.getLogger(FileshareAdminExceptionHandlerImpl.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler#handleException(java.lang.Exception)
     */
    @Override
    public void handleException ( Exception e ) {
        if ( e instanceof FileshareException && e.getCause() instanceof AgentCommunicationErrorException ) {
            log.info(e.getClass().getName());
            handleCommError((AgentCommunicationErrorException) e.getCause());
            return;
        }

        if ( e instanceof FileshareException ) {
            handleFileshareException((FileshareException) e);
            return;
        }

        ExceptionHandler.handle(e);
    }


    /**
     * @param e
     */
    private static void handleFileshareException ( FileshareException e ) {
        if ( e instanceof UserExistsException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "userExists"); //$NON-NLS-1$
        }
        else if ( e instanceof UserLimitExceededException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "userLimitExceeded"); //$NON-NLS-1$
        }
        else {
            ExceptionHandler.handle(e);
        }
    }


    private static void addMessage ( FacesMessage.Severity severity, String msgId, Object... args ) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, makeMessageFormat(msgId, args), StringUtils.EMPTY));
    }


    /**
     * @param msgId
     * @param args
     * @return
     */
    private static String makeMessageFormat ( String msgId, Object[] args ) {
        return FileshareOrchGUIMessages.format("exception." + msgId, args); //$NON-NLS-1$
    }


    /**
     * @param cause
     */
    private static void handleCommError ( AgentCommunicationErrorException e ) {
        ExceptionHandler.handle(e);
    }

}
