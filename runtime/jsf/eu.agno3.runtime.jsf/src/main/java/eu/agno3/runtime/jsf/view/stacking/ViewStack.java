/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;


/**
 * @author mbechler
 * 
 */
public final class ViewStack implements Serializable {

    private static final String STACK_ID_COLLISION = "Stack Id collision %s (window: %s)"; //$NON-NLS-1$
    private static final String NO_STACK_ENTRY = "Failed to locate stack entry with id %s (window: %s)"; //$NON-NLS-1$
    private static final String SESSION_KEY = "eu.agno3.jsf.viewStack"; //$NON-NLS-1$
    private static final String LAST_USED_KEY = "eu.agno3.jsf.viewStack_lastUsed"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ViewStack.class);

    /**
     * 
     */
    private static final long serialVersionUID = -7586393184324622561L;

    /**
     * 
     */
    private static final int DIALOG_TIMEOUT_MINUTES = 10;

    private final Map<String, StackEntry> entries = new HashMap<>();
    private final Map<String, ReturnEntry> returnValues = new HashMap<>();


    /**
     * 
     */
    private ViewStack () {}


    /**
     * @param id
     * @return the stack entry for the id
     */
    public StackEntry getEntry ( String id ) {
        StackEntry e = this.entries.get(id);

        if ( e == null ) {
            throw new ViewStackException(
                String.format(NO_STACK_ENTRY, id, FacesContext.getCurrentInstance().getExternalContext().getClientWindow().getId()));
        }

        return e;
    }


    /**
     * @param id
     * @return the stack entry for the id
     */
    public StackEntry popEntry ( String id ) {
        StackEntry e = this.entries.remove(id);
        this.returnValues.remove(id);

        if ( e == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No return entry found for " + id); //$NON-NLS-1$
            }
        }

        return e;
    }


    /**
     * @param id
     * @param e
     */
    public void pushEntry ( String id, StackEntry e ) {
        if ( this.entries.put(id, e) != null ) {
            throw new ViewStackException(
                String.format(STACK_ID_COLLISION, id, FacesContext.getCurrentInstance().getExternalContext().getClientWindow().getId()));
        }
    }


    /**
     * @param context
     * @return the ViewStack instance for the given context
     */
    public static ViewStack getViewStack ( ExternalContext context ) {
        Map<String, Object> session = context.getSessionMap();

        String sessionKey = SESSION_KEY;
        String lastUsedKey = LAST_USED_KEY;
        if ( context.getClientWindow() != null && !StringUtils.isBlank(context.getClientWindow().getId()) ) {
            String id = context.getClientWindow().getId();
            if ( log.isTraceEnabled() ) {
                log.trace("Client window is " + id); //$NON-NLS-1$
            }

            if ( id.startsWith("dlg_") ) { //$NON-NLS-1$
                id = id.substring(4);
            }

            sessionKey += "_" + id; //$NON-NLS-1$
            lastUsedKey += "_" + id; //$NON-NLS-1$
        }

        DateTime lastUsed = (DateTime) session.get(lastUsedKey);
        if ( lastUsed != null && log.isTraceEnabled() ) {
            log.trace("Last used " + lastUsed); //$NON-NLS-1$
        }

        session.put(lastUsedKey, DateTime.now());
        Object obj = session.get(sessionKey);
        if ( obj == null ) {
            log.debug("Creating new view stack"); //$NON-NLS-1$
            obj = new ViewStack();
            session.put(sessionKey, obj);
        }
        return (ViewStack) obj;
    }


    /**
     * 
     * Removed dialog context state that has not been used for DIALOG_TIMEOUT_MINUTES
     * 
     * @param context
     */
    public static void cleanupExpired ( FacesContext context ) {
        Map<String, Object> session = context.getExternalContext().getSessionMap();
        DateTime timeout = DateTime.now().minusMinutes(DIALOG_TIMEOUT_MINUTES);

        Set<String> removeWindowIds = new HashSet<>();
        for ( Entry<String, Object> e : session.entrySet() ) {
            if ( !e.getKey().startsWith(LAST_USED_KEY + "_") ) { //$NON-NLS-1$
                continue;
            }

            String windowId = e.getKey().substring(LAST_USED_KEY.length() + 1);
            DateTime lastUsed = (DateTime) e.getValue();
            ViewStack stack = (ViewStack) session.get(SESSION_KEY + '_' + windowId);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Found dialog context for windows %s last used %s e %d rv %d", //$NON-NLS-1$
                    windowId,
                    lastUsed,
                    stack != null ? stack.entries.size() : 0,
                    stack != null ? stack.returnValues.size() : 0));
            }

            if ( lastUsed.isBefore(timeout) ) {
                removeWindowIds.add(windowId);
            }
        }

        for ( String remove : removeWindowIds ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Expiring dialog state for window " + remove); //$NON-NLS-1$
            }
            session.remove(SESSION_KEY + "_" + remove); //$NON-NLS-1$
            session.remove(LAST_USED_KEY + "_" + remove); //$NON-NLS-1$
        }
    }


    /**
     * @param id
     * @param data
     */
    public void pushReturnValue ( String id, ReturnEntry data ) {

        if ( id == null ) {
            throw new ViewStackException("Return pointer is NULL"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("pushReturnValue for " + id); //$NON-NLS-1$
        }

        if ( this.returnValues.put(id, data) != null ) {
            log.warn(String.format(STACK_ID_COLLISION, id, FacesContext.getCurrentInstance().getExternalContext().getClientWindow().getId()));
        }
    }


    /**
     * 
     * @param id
     * @return the returned value or null if none exists
     */
    public ReturnEntry popReturnValue ( String id ) {
        if ( log.isDebugEnabled() ) {
            log.debug("popReturnValue for " + id); //$NON-NLS-1$
        }
        return this.returnValues.remove(id);
    }


    /**
     * Pop from stack until the current top of stack is the given frame
     * 
     * @param currentId
     * @param targetReturnToId
     * @return the target entry
     */
    public StackEntry popTo ( String currentId, String targetReturnToId ) {
        StackEntry e = this.getEntry(currentId);

        while ( e.getParentId() != null && !e.getId().equals(targetReturnToId) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Popping frame " + e.getId()); //$NON-NLS-1$
            }
            this.popEntry(e.getId());
            e = this.getEntry(e.getParentId());
        }

        return e;
    }


    /**
     * @return whether the view stack is empty
     */
    public boolean isEmpty () {
        return this.entries.isEmpty();
    }


    /**
     * 
     */
    public void clear () {
        this.entries.clear();
    }
}
