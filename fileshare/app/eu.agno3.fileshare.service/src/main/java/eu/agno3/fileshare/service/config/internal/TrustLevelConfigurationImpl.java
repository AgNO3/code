/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.fileshare.service.config.TrustLevelConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustLevelConfiguration.class )
public class TrustLevelConfigurationImpl implements TrustLevelConfiguration {

    private static final Logger log = Logger.getLogger(TrustLevelConfigurationImpl.class);

    private Map<String, TrustLevel> levels = new HashMap<>();
    private List<TrustLevel> sorted = Collections.EMPTY_LIST;


    @Reference ( updated = "modifiedTrustLevel", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindTrustLevel ( TrustLevel l ) {
        this.levels.put(l.getId(), l);
        updatedTrustLevels();
    }


    protected synchronized void modifiedTrustLevel ( TrustLevel l ) {
        updatedTrustLevels();
    }


    protected synchronized void unbindTrustLevel ( TrustLevel l ) {
        this.levels.remove(l.getId());
        updatedTrustLevels();
    }


    /**
     * 
     */
    private void updatedTrustLevels () {
        List<TrustLevel> s = new ArrayList<>(this.levels.values());
        Collections.sort(s, new TrustLevelComparator());
        this.sorted = s;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.TrustLevelConfiguration#getTrustLevel(java.lang.String)
     */
    @Override
    public TrustLevel getTrustLevel ( String level ) {
        return this.levels.get(level);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.TrustLevelConfiguration#getTrustLevel(eu.agno3.fileshare.model.Subject)
     */
    @Override
    public TrustLevel getTrustLevel ( Subject s ) {

        if ( s instanceof Group ) {
            return getGroupTrustLevel((Group) s);
        }

        for ( TrustLevel l : this.sorted ) {
            if ( l.match(s) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Matched trust level " + l); //$NON-NLS-1$
                }
                return l;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("NOT matched trust levels " + this.levels); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * 
     * @param g
     * @return the groups trust level
     */
    @Override
    public TrustLevel getGroupTrustLevel ( Group g ) {
        for ( TrustLevel l : this.sorted ) {
            if ( l.matchGroup(g) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Matched trust level " + l); //$NON-NLS-1$
                }
                return l;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("NOT matched trust levels " + this.levels); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.TrustLevelConfiguration#getLinkTrustLevel()
     */
    @Override
    public TrustLevel getLinkTrustLevel () {
        for ( TrustLevel l : this.sorted ) {
            if ( l.matchLink() ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Matched trust level " + l); //$NON-NLS-1$
                }
                return l;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("NOT matched trust levels " + this.levels); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.TrustLevelConfiguration#getMailTrustLevel(java.lang.String)
     */
    @Override
    public TrustLevel getMailTrustLevel ( String mailAddress ) {
        for ( TrustLevel l : this.sorted ) {
            if ( l.matchMail(mailAddress) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Matched trust level " + l); //$NON-NLS-1$
                }
                return l;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("NOT matched trust levels " + this.levels); //$NON-NLS-1$
        }

        return null;
    }

}
