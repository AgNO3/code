/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logsink.LogAction;
import eu.agno3.orchestrator.system.logsink.LogProcessorPlugin;
import eu.agno3.orchestrator.system.logsink.ProcessorContext;
import eu.agno3.orchestrator.system.logsink.impl.AbstractRecombiningLogProcessPlugin;


/**
 * @author mbechler
 *
 */
@Component ( service = LogProcessorPlugin.class )
public class KernelRecombiningLogProcessorPlugin extends AbstractRecombiningLogProcessPlugin {

    private static final Logger log = Logger.getLogger(KernelRecombiningLogProcessorPlugin.class);

    private static String BACKTRACE_START = "Call Trace:"; //$NON-NLS-1$
    private static String CUT_MARKER = "------------[ cut here ]------------"; //$NON-NLS-1$
    private static Pattern BACKTRACE_PATTERN = Pattern.compile("^\\s*\\[\\<?[0-9a-f]{16}\\>?\\]\\s.*$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static Pattern CODE_PATTERN = Pattern.compile("^\\s*([0-9a-f]{16})(\\s+[0-9a-f]{16})*$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static Pattern CUT_END_PATTERN = Pattern.compile("^---\\[ end\\s.*\\]---$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private int state = 0;

    private Map<String, Object> baseMessage;
    private StringBuilder backtraceBuilder = new StringBuilder();

    private boolean useCutMarkers = true;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#getPriority()
     */
    @Override
    public float getPriority () {
        return 100f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#matches(java.util.Map)
     */
    @Override
    public boolean matches ( Map<String, Object> ev ) {
        return super.matches(ev) && "kernel".equals( //$NON-NLS-1$
            ev.get("_TRANSPORT")); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.impl.AbstractRecombiningLogProcessPlugin#flushAll(eu.agno3.orchestrator.system.logsink.ProcessorContext)
     */
    @Override
    protected void flushAll ( ProcessorContext context ) {
        flushRecombined(context);
        super.flushAll(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.impl.AbstractRecombiningLogProcessPlugin#haveMessage(java.util.Map)
     */
    @Override
    protected LogAction haveMessage ( ProcessorContext ctx, Map<String, Object> ev ) {

        if ( log.isTraceEnabled() ) {
            log.trace("State is " + this.state); //$NON-NLS-1$
        }

        String msg = (String) ev.get(LogFields.MESSAGE);
        if ( StringUtils.isBlank(msg) ) {
            log.trace("No message"); //$NON-NLS-1$
            return LogAction.IGNORE;
        }

        if ( this.useCutMarkers ) {
            LogAction res = handleCutMarkers(ctx, ev, msg);
            if ( res != LogAction.EMIT ) {
                return res;
            }
        }
        else if ( CUT_MARKER.equalsIgnoreCase(msg) || CUT_END_PATTERN.matcher(msg).matches() ) {
            return LogAction.DROP;
        }

        if ( this.getBacklog().isEmpty() && this.baseMessage == null ) {
            log.trace("Not yet having a message"); //$NON-NLS-1$
            return LogAction.EMIT;
        }

        if ( this.baseMessage == null ) {
            Map<String, Object> element = this.getBacklog().poll();

            if ( element.containsKey(LogFields.RECOMBINED) && (boolean) element.get(LogFields.RECOMBINED) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Already recombined " + element); //$NON-NLS-1$
                }
                return LogAction.IGNORE;
            }

            element.put(LogFields.RECOMBINED, true);

            this.baseMessage = new HashMap<>(element);

            this.state = 0;
            this.backtraceBuilder.delete(0, this.backtraceBuilder.length());
            this.backtraceBuilder.append(this.baseMessage.get(LogFields.MESSAGE)).append('\n');
        }

        if ( log.isTraceEnabled() ) {
            log.trace(this.state + " " + msg); //$NON-NLS-1$
        }

        if ( ( ( this.state == 0 || this.state == 1 ) && CODE_PATTERN.matcher(msg).matches() ) || ( this.state == 1 && StringUtils.isBlank(msg) ) ) {
            log.debug("Found code dump"); //$NON-NLS-1$
            this.state = 1;
            this.backtraceBuilder.append(msg).append('\n');
            return LogAction.DROP;
        }
        else if ( ( this.state == 0 || this.state == 1 ) && BACKTRACE_START.equalsIgnoreCase(msg) ) {
            log.debug("Found backtrace start"); //$NON-NLS-1$
            this.state = 2;
            this.backtraceBuilder.append(msg).append('\n');
            return LogAction.DROP;
        }
        else if ( this.state == 2 && BACKTRACE_PATTERN.matcher(msg).matches() ) {
            log.debug("Found backtrace " + msg); //$NON-NLS-1$
            this.backtraceBuilder.append(msg).append('\n');
            return LogAction.DROP;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unmatched '%s' state %d backlog %d", msg, this.state, this.getBacklog().size())); //$NON-NLS-1$
        }

        flushRecombined(ctx);
        return LogAction.EMIT;
    }


    /**
     * @param ctx
     * @param ev
     * @param msg
     * @return
     */
    private LogAction handleCutMarkers ( ProcessorContext ctx, Map<String, Object> ev, String msg ) {
        if ( ( this.state == 0 || this.state == -1 ) && CUT_MARKER.equalsIgnoreCase(msg) ) {

            log.debug("Found cut marker"); //$NON-NLS-1$
            if ( this.baseMessage != null ) {
                flushRecombined(ctx);
            }

            if ( ev.containsKey(LogFields.RECOMBINED) && (boolean) ev.get(LogFields.RECOMBINED) ) {
                return LogAction.IGNORE;
            }

            ev.put(LogFields.RECOMBINED, true);
            this.backtraceBuilder.delete(0, this.backtraceBuilder.length());
            this.baseMessage = new HashMap<>(ev);
            this.state = -1;
            return LogAction.DROP;
        }
        else if ( this.state == -1 && !CUT_END_PATTERN.matcher(msg).matches() ) {
            log.debug("Found data marker " + msg); //$NON-NLS-1$
            this.backtraceBuilder.append(msg).append('\n');
            return LogAction.DROP;
        }
        else if ( this.state == -1 ) {
            log.debug("Found end marker " + msg); //$NON-NLS-1$
            this.backtraceBuilder.append(msg).append('\n');
            flushRecombined(ctx);
            return LogAction.DROP;
        }

        return LogAction.EMIT;
    }


    /**
     * 
     */
    private synchronized void flushRecombined ( ProcessorContext ctx ) {
        if ( this.baseMessage != null ) {
            String string = this.backtraceBuilder.toString();
            if ( string.isEmpty() ) {
                this.baseMessage = null;
                this.state = 0;
                return;
            }
            if ( string.charAt(string.length() - 1) == '\n' ) {
                string = string.substring(0, string.length() - 1);
            }
            this.baseMessage.put(LogFields.MESSAGE, string);
            if ( log.isDebugEnabled() ) {
                log.debug("Recombined message " + this.baseMessage); //$NON-NLS-1$
            }
            log.debug("Resetting base message"); //$NON-NLS-1$

            Map<String, Object> msg = this.baseMessage;
            this.baseMessage = null;
            this.state = 0;
            ctx.inject(msg);
        }
        else {
            this.baseMessage = null;
            this.state = 0;
        }
    }

}
