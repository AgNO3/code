/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import java.util.UUID;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.component.auth.AgentComponentPrincipal;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.gui.component.auth.GuiComponentPrincipal;
import eu.agno3.orchestrator.gui.msg.addressing.GuiMessageSource;
import eu.agno3.orchestrator.server.component.auth.AuthConstants;
import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;
import eu.agno3.orchestrator.server.component.auth.ServerComponentPrincipal;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 *
 */
public final class PrincipalFactory {

    /**
     * 
     */
    private PrincipalFactory () {}


    /**
     * @param dn
     * @return a component principal for the X509 subject
     */
    public static ComponentPrincipal getComponentPrincipal ( X500Name dn ) {
        RDN[] agentRdn = dn.getRDNs(AuthConstants.AGENT_ID_OID);
        RDN[] guiRdn = dn.getRDNs(AuthConstants.GUI_ID_OID);
        RDN[] serverRdn = dn.getRDNs(AuthConstants.SERVER_ID_OID);

        ComponentPrincipal princ;
        if ( PrincipalFactory.validRDN(serverRdn) ) {
            princ = new ServerComponentPrincipal(PrincipalFactory.extractComponentId(serverRdn));
        }
        else if ( PrincipalFactory.validRDN(agentRdn) ) {
            princ = new AgentComponentPrincipal(PrincipalFactory.extractComponentId(agentRdn));
        }
        else if ( PrincipalFactory.validRDN(guiRdn) ) {
            princ = new GuiComponentPrincipal(PrincipalFactory.extractComponentId(guiRdn));
        }
        else {
            throw new SecurityException("Invalid component certificate DN"); //$NON-NLS-1$
        }
        return princ;
    }


    private static @NonNull UUID extractComponentId ( RDN[] serverRdn ) {
        UUID fromRDN = UUID.fromString(serverRdn[ 0 ].getFirst().getValue().toString());

        if ( fromRDN == null ) {
            throw new SecurityException("Invalid UUID"); //$NON-NLS-1$
        }

        return fromRDN;
    }


    private static boolean validRDN ( RDN[] rdns ) {
        return rdns != null && rdns.length == 1 && !rdns[ 0 ].isMultiValued();
    }


    /**
     * @param source
     * @return the principal for the message source
     */
    public static ComponentPrincipal fromMessageSource ( MessageSource source ) {
        if ( source instanceof AgentMessageSource ) {
            return new AgentComponentPrincipal( ( (AgentMessageSource) source ).getAgentId());
        }
        else if ( source instanceof ServerMessageSource ) {
            return new ServerComponentPrincipal( ( (ServerMessageSource) source ).getServerId());
        }
        else if ( source instanceof GuiMessageSource ) {
            return new GuiComponentPrincipal( ( (GuiMessageSource) source ).getGuiId());
        }
        else {
            throw new SecurityException("Failed to get principal from message source " + source); //$NON-NLS-1$
        }
    }


    /**
     * @param userName
     * @return a component principal for the user name
     */
    public static ComponentPrincipal fromUserName ( String userName ) {

        if ( userName.startsWith(AgentComponentPrincipal.AGENT_USER_PREFIX) ) {
            return fromAgentPrincipal(userName);
        }
        else if ( userName.startsWith(ServerComponentPrincipal.SERVER_USER_PREFIX) ) {
            return fromServerPrincipal(userName);
        }
        else if ( userName.startsWith(GuiComponentPrincipal.GUI_USER_PREFIX) ) {
            return fromGuiPrincipal(userName);
        }
        else if ( userName.equals(AuthConstants.SYSTEM_USER) ) {
            throw new SecurityException("Auth attempt with system user"); //$NON-NLS-1$
        }

        throw new SecurityException("Failed to get principal from username"); //$NON-NLS-1$
    }


    /**
     * @param userName
     * @return
     */
    private static ComponentPrincipal fromGuiPrincipal ( String userName ) {
        UUID fromString = UUID.fromString(userName.substring(GuiComponentPrincipal.GUI_USER_PREFIX.length()));
        if ( fromString == null ) {
            throw new SecurityException();
        }
        return new GuiComponentPrincipal(fromString);
    }


    /**
     * @param userName
     * @return
     */
    private static ComponentPrincipal fromServerPrincipal ( String userName ) {
        UUID fromString = UUID.fromString(userName.substring(ServerComponentPrincipal.SERVER_USER_PREFIX.length()));
        if ( fromString == null ) {
            throw new SecurityException();
        }
        return new ServerComponentPrincipal(fromString);
    }


    /**
     * @param userName
     * @return
     */
    private static ComponentPrincipal fromAgentPrincipal ( String userName ) {
        UUID fromString = UUID.fromString(userName.substring(AgentComponentPrincipal.AGENT_USER_PREFIX.length()));
        if ( fromString == null ) {
            throw new SecurityException();
        }
        return new AgentComponentPrincipal(fromString);
    }

}
