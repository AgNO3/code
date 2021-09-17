/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui;


import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.webbeans.conversation.ConversationImpl;

import eu.agno3.orchestrator.server.session.service.SessionService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "pingBean" )
@RequestScoped
public class PingBean {

    private static final Logger log = Logger.getLogger(PingBean.class);

    @Inject
    private Conversation c;

    @Inject
    private ServerServiceProvider ssp;


    /**
     */
    public void ping () {
        log.trace("Ping"); //$NON-NLS-1$
        log.trace(SecurityUtils.getSubject().getSession().getLastAccessTime());
        log.trace(SecurityUtils.getSubject().getSession().getStartTimestamp());

        if ( log.isTraceEnabled() && !this.c.isTransient() ) {
            log.trace("Conversation " + this.c.getId()); //$NON-NLS-1$
            log.trace("Conversation timeout " + this.c.getTimeout()); //$NON-NLS-1$

        }
        if ( !this.c.isTransient() && this.c instanceof ConversationImpl ) {
            ConversationImpl ci = (ConversationImpl) this.c;
            log.trace("Last access " + ci.getLastAccessTime()); //$NON-NLS-1$
            ci.updateLastAccessTime();
        }

        try {
            SessionService service = this.ssp.getService(SessionService.class);
            if ( service != null ) {
                service.keepAlive();
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }

}
