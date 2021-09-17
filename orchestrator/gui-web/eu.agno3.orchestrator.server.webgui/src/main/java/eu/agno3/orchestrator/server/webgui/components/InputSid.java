/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.util.List;

import javax.el.MethodExpression;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.realms.RealmEntityType;
import eu.agno3.orchestrator.realms.RealmLookupResult;


/**
 * @author mbechler
 *
 */
public class InputSid extends UIInput implements NamingContainer {

    private static final Logger log = Logger.getLogger(InputSid.class);

    /**
     * 
     */
    private static final String RID = "rid"; //$NON-NLS-1$
    private static final String BASESID = "baseSid"; //$NON-NLS-1$

    private transient String cachedFor;
    private transient boolean nameLoaded;
    private transient RealmLookupResult cachedResult;


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#clearInitialState()
     */
    @Override
    public void clearInitialState () {
        super.clearInitialState();
        this.cachedResult = null;
        this.cachedFor = null;
        this.nameLoaded = false;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    public boolean isDomainSID () {
        return (boolean) this.getAttributes().getOrDefault("domainSid", false); //$NON-NLS-1$

    }


    /**
     * @return the cachedResult
     */
    public RealmLookupResult getCachedResult () {
        if ( !this.nameLoaded ) {
            this.nameLoaded = true;
            updateName();
        }
        return this.cachedResult;
    }


    public String iconForType ( Object o ) {
        if ( ! ( o instanceof RealmEntityType ) ) {
            return "ui-icon-blank"; //$NON-NLS-1$
        }

        RealmEntityType t = (RealmEntityType) o;

        switch ( t ) {
        case GROUP:
            return "ui-icon-group"; //$NON-NLS-1$
        case USER:
            return "ui-icon-person"; //$NON-NLS-1$
        case DOMAIN:
            return "ui-icon-home"; //$NON-NLS-1$
        default:
            return "ui-icon-blank"; //$NON-NLS-1$
        }
    }


    public String getRid () {
        if ( isDomainSID() ) {
            return StringUtils.EMPTY;
        }

        String rid = (String) this.getStateHelper().get(RID);

        if ( !StringUtils.isBlank(rid) ) {
            return rid;
        }

        String sidVal = (String) getValue();
        if ( StringUtils.isBlank(sidVal) ) {
            return StringUtils.EMPTY;
        }

        int lastSep = sidVal.lastIndexOf('-');
        return sidVal.substring(Math.min(sidVal.length() - 1, lastSep + 1));
    }


    public void setRid ( String rid ) {
        if ( !StringUtils.isBlank(rid) ) {
            this.getStateHelper().put(RID, rid);
        }
    }


    public String getBaseSid () {
        String baseSid = (String) this.getStateHelper().get(BASESID);

        if ( !StringUtils.isBlank(baseSid) ) {
            return baseSid;
        }

        String sidVal = (String) getValue();
        if ( StringUtils.isBlank(sidVal) ) {
            return null;
        }

        if ( sidVal.startsWith("S-1-") ) { //$NON-NLS-1$
            sidVal = sidVal.substring(4);
        }

        if ( isDomainSID() ) {
            return sidVal;
        }

        int lastSep = sidVal.lastIndexOf('-');
        return sidVal.substring(0, lastSep);
    }


    public void setBaseSid ( String baseSid ) {
        String actual = baseSid;
        if ( StringUtils.isBlank(actual) ) {
            return;
        }
        if ( actual.startsWith("S-1-") ) { //$NON-NLS-1$
            actual = actual.substring(4);
        }

        if ( !isDomainSID() && StringUtils.isBlank(getRid()) ) {
            int lastSep = actual.lastIndexOf('-');
            if ( lastSep < 0 ) {
                this.getStateHelper().put(BASESID, actual);
            }
            else {
                this.getStateHelper().put(BASESID, actual.substring(0, lastSep));
                this.getStateHelper().put(RID, actual.substring(Math.min(actual.length() - 1, lastSep + 1)));
            }
        }
        else {
            this.getStateHelper().put(BASESID, actual);
        }
    }


    public String makeSid () {
        String base = getBaseSid();

        if ( StringUtils.isBlank(base) ) {
            return null;
        }

        String rid = this.getRid();
        return String.format(
            "S-1-%s%s", //$NON-NLS-1$
            base,
            ( isDomainSID() || StringUtils.isBlank(rid) ) ? StringUtils.EMPTY : "-" + rid); //$NON-NLS-1$
    }


    public String getAutoCompleteSelection () {
        return null;
    }


    public void setAutoCompleteSelection ( String selection ) {}


    public void onSelect ( SelectEvent ev ) {
        Object object = ev.getObject();

        if ( ! ( object instanceof String ) ) {
            return;
        }

        String sid = (String) object;
        if ( log.isDebugEnabled() ) {
            log.debug("Have selection " + object); //$NON-NLS-1$
        }
        setSubmittedValue(sid);
        setValue(sid);
        this.getStateHelper().remove(RID);
        this.getStateHelper().remove(BASESID);
        updateName();
    }


    @SuppressWarnings ( "unchecked" )
    public List<RealmLookupResult> complete ( String pattern ) {

        Object v = getAttributes().get("search"); //$NON-NLS-1$
        String domainHint = (String) this.getAttributes().get("domainHint"); //$NON-NLS-1$
        if ( ! ( v instanceof MethodExpression ) || StringUtils.isBlank(domainHint) ) {
            return null;
        }

        MethodExpression me = (MethodExpression) v;
        List<RealmLookupResult> res = (List<RealmLookupResult>) me.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
            domainHint, pattern
        });

        if ( log.isDebugEnabled() ) {
            log.debug("Found " + res); //$NON-NLS-1$
        }
        return res;
    }


    /**
     * Refresh cached name
     */
    public void updateName () {
        updateName(makeSid());
    }


    /**
     * @param sid
     */
    private void updateName ( String sid ) {
        if ( StringUtils.countMatches(sid, '-') < 3 ) {
            return;
        }
        if ( this.cachedFor == null || !this.cachedFor.equals(sid) ) {
            String domainHint = (String) this.getAttributes().get("domainHint"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Updating name for " + sid); //$NON-NLS-1$
                log.debug("Domain hint is " + domainHint); //$NON-NLS-1$
            }

            this.cachedResult = lookupName(domainHint, sid);
            this.cachedFor = sid;
        }
    }


    /**
     * @param domainHint
     * @param sid
     * @return
     */
    private RealmLookupResult lookupName ( String domainHint, String sid ) {
        Object v = getAttributes().get("lookupSID"); //$NON-NLS-1$
        if ( ! ( v instanceof MethodExpression ) ) {
            return null;
        }

        MethodExpression me = (MethodExpression) v;
        return (RealmLookupResult) me.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
            domainHint, sid
        });
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);

        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext ctx ) {
        String sid = makeSid();
        setValue(sid);
        updateName(sid);
        super.updateModel(ctx);
    }

}
