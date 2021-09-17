/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.logs;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.prefs.LocaleSettingsBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "logUtils" )
public class LogUtils {

    private static final Logger log = Logger.getLogger(LogUtils.class);

    private static EventSeverity[] DISPLAY_SEVERITIES = Arrays.copyOfRange(EventSeverity.values(), 1, EventSeverity.values().length);

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private StructureUtil structureUtil;

    @Inject
    private LocaleSettingsBean localeSettings;

    private static final Set<String> HIDDEN_PROPERTIES = new HashSet<>(Arrays.asList(
        "_CAP_EFFECTIVE", //$NON-NLS-1$
        "BUNDLE_ID", //$NON-NLS-1$
        "CODE_FILE", //$NON-NLS-1$
        "_COMM", //$NON-NLS-1$
        "SYSLOG_PID", //$NON-NLS-1$
        "_AUDIT_SESSION", //$NON-NLS-1$
        "_AUDIT_LOGINUID", //$NON-NLS-1$
        "_SYSTEMD_CGROUP", //$NON-NLS-1$
        "_SYSTEMD_OWNER_UID")); //$NON-NLS-1$


    public boolean isPropertyHidden ( String property ) {
        return HIDDEN_PROPERTIES.contains(property);
    }


    public String getHostName ( MapEvent ev ) {
        UUID objId = getObjectId(ev);
        if ( objId == null ) {
            return StringUtils.EMPTY;
        }

        try {
            StructuralObject byId = this.structureCache.getById(objId);
            if ( byId instanceof ServiceStructuralObject ) {
                byId = this.structureCache.getParentFor(byId);
            }
            return this.structureUtil.getObjectShortName(byId);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    public String getObjectName ( MapEvent ev ) {

        UUID objId = getObjectId(ev);
        if ( objId == null ) {
            return StringUtils.EMPTY;
        }

        try {
            StructuralObject byId = this.structureCache.getById(objId);
            return this.structureUtil.getObjectDescriptiveName(byId);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }

    }


    public String formatTimestamp ( Long ts ) {
        if ( ts == null ) {
            return null;
        }

        String patternForStyle = DateTimeFormat.patternForStyle("S-", this.localeSettings.getDateLocale()); //$NON-NLS-1$
        return DateTimeFormat.forPattern(patternForStyle + " HH:mm:ss.SSS").print(ts); //$NON-NLS-1$
    }


    public EventSeverity[] getSeverities () {
        return DISPLAY_SEVERITIES;
    }


    public boolean filterSeverity ( Object val, Object filter, Locale l ) {
        log.info(val);
        log.info(filter);

        if ( ! ( val instanceof Event ) ) {
            return false;
        }
        else if ( ! ( filter instanceof EventSeverity ) ) {
            return true;
        }

        Event ev = (Event) val;
        EventSeverity sev = (EventSeverity) filter;

        return ev.getSeverity().ordinal() >= sev.ordinal();
    }


    /**
     * @param ev
     * @return
     */
    private static UUID getObjectId ( MapEvent ev ) {
        Object id = ev.get("objectId"); //$NON-NLS-1$

        UUID objId = null;
        if ( id instanceof String ) {
            objId = UUID.fromString((String) id);
        }
        else if ( id instanceof UUID ) {
            objId = (UUID) id;
        }
        return objId;
    }


    public String getObjectIcon ( MapEvent ev ) {

        UUID objId = getObjectId(ev);
        if ( objId == null ) {
            return "ui-icon-blank"; //$NON-NLS-1$
        }

        try {
            StructuralObject byId = this.structureCache.getById(objId);
            return this.structureUtil.getObjectIcon(byId);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return "ui-icon-blank"; //$NON-NLS-1$
    }


    public String getPropertyLabel ( String property ) {
        String key = "log.property." + property; //$NON-NLS-1$
        if ( GuiMessages.contains(key) ) {
            return GuiMessages.get(key);
        }
        return property;

    }
}
