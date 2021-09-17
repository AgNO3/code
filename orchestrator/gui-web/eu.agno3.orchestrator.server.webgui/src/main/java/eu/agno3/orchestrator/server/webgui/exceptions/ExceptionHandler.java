/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.exceptions;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.RemoteCallErrorException;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;


/**
 * @author mbechler
 *
 */
public class ExceptionHandler {

    private static final Logger log = Logger.getLogger(ExceptionHandler.class);


    public static void handle ( Exception e ) {

        if ( e instanceof UndeclaredThrowableException && e.getCause() instanceof InvocationTargetException
                && e.getCause().getCause() instanceof Exception ) {
            handle((Exception) e.getCause().getCause());
        }
        else if ( e instanceof SOAPFaultException ) {
            SOAPFaultException se = (SOAPFaultException) e;
            if ( se.getCause() instanceof Exception ) {
                handle((Exception) se.getCause());
                return;
            }
            unhandledException(se);
        }
        else if ( e instanceof AbstractModelException ) {
            handleModelException((AbstractModelException) e);
        }
        else if ( e instanceof JobQueueException ) {
            handleJobException((JobQueueException) e);
        }
        else if ( e instanceof AgentException ) {
            handleRemoteError((AgentException) e);
        }
        else if ( e instanceof SecurityManagementException ) {
            handleSecurityManagementException((SecurityManagementException) e);
        }
        else if ( e instanceof Fault ) {
            Fault f = (Fault) e;
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Fault %d/%s: %s", f.getStatusCode(), f.getCode(), f.getDetail(), e.getCause())); //$NON-NLS-1$
            }
            if ( f.getStatusCode() >= 400 && f.getStatusCode() < 500 ) {
                SecurityUtils.getSubject().logout();
                throw new AuthenticationException("Webservice authentication failure", e); //$NON-NLS-1$
            }
            addMessage(
                FacesMessage.SEVERITY_ERROR,
                "webservice.http.error", //$NON-NLS-1$
                f.getStatusCode());
        }
        else if ( e instanceof SessionException ) {
            throw new AuthenticationException("Authentication failed", e); //$NON-NLS-1$
        }
        else {
            unhandledException(e);
        }
    }


    /**
     * @param e
     */
    private static void handleModelException ( AbstractModelException e ) {
        ModelExceptionHandler.handleException(StringUtils.EMPTY, e);
    }


    /**
     * @param e
     */
    private static void handleRemoteError ( AgentException e ) {

        if ( e instanceof RemoteCallErrorException ) {
            addMessage(
                FacesMessage.SEVERITY_ERROR,
                "remote.comm.exception", //$NON-NLS-1$
                getMessage(e),
                e.getFaultInfo() != null ? e.getFaultInfo().getInstanceName() : StringUtils.EMPTY);
        }
        else if ( e instanceof AgentOfflineException ) {
            addMessage(
                FacesMessage.SEVERITY_ERROR,
                "remote.agent.offline", //$NON-NLS-1$
                getMessage(e),
                e.getFaultInfo() != null ? e.getFaultInfo().getInstanceName() : StringUtils.EMPTY);
        }
        else {
            addMessage(
                FacesMessage.SEVERITY_ERROR,
                "remote.comm.error", //$NON-NLS-1$
                getMessage(e),
                e.getFaultInfo() != null ? e.getFaultInfo().getInstanceName() : StringUtils.EMPTY);
        }
    }


    /**
     * @param e
     */
    private static void handleJobException ( JobQueueException e ) {
        if ( e instanceof JobUnknownException ) {
            addMessage(
                FacesMessage.SEVERITY_ERROR,
                "job.unknown", //$NON-NLS-1$
                getMessage(e));
        }
        else {
            addMessage(
                FacesMessage.SEVERITY_ERROR,
                "job.error", //$NON-NLS-1$
                getMessage(e));
        }
    }


    /**
     * @param e
     */
    private static void handleSecurityManagementException ( SecurityManagementException e ) {
        if ( e instanceof UserLicenseLimitExceededException ) {
            addMessage(FacesMessage.SEVERITY_FATAL, "userLicenseExceeded"); //$NON-NLS-1$
        }
        else {
            unhandledException(e);
        }
    }


    private static String getMessage ( Exception e ) {
        if ( StringUtils.isBlank(e.getMessage()) ) {
            return e.getClass().getName();
        }
        return e.getMessage();
    }


    /**
     * @param e
     */
    private static void unhandledException ( Exception e ) {
        log.error("Unhandled exception", e); //$NON-NLS-1$
        addMessage(FacesMessage.SEVERITY_FATAL, "unhandled", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()); //$NON-NLS-1$
    }


    /**
     * @param severity
     * @param msgId
     * @param conflictingName
     */
    static void addMessage ( Severity severity, String msgId, Object... args ) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, makeMessageFormat(msgId, args), StringUtils.EMPTY));
    }


    /**
     * @param msgId
     * @param args
     * @return
     */
    private static String makeMessageFormat ( String msgId, Object[] args ) {
        return GuiMessages.format("exception." + msgId, args); //$NON-NLS-1$
    }

}
