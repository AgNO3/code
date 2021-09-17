/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.fileshare.orch.common.config.FileshareUserTrustLevel;
import eu.agno3.fileshare.orch.common.config.FileshareUserTrustLevelConfig;
import eu.agno3.fileshare.orch.common.config.FileshareUserTrustLevelImpl;
import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fs_trustLevelBean" )
public class TrustLevelBean extends AbstractConfigObjectBean<FileshareUserTrustLevel, FileshareUserTrustLevelImpl> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return FileshareConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<FileshareUserTrustLevel> getObjectType () {
        return FileshareUserTrustLevel.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<FileshareUserTrustLevelImpl> getInstanceType () {
        return FileshareUserTrustLevelImpl.class;
    }


    /**
     * @return comparator
     */
    public Comparator<FileshareUserTrustLevel> getComparator () {
        return new TrustLevelComparator();
    }


    /**
     * @param wr
     * @return a trust level completer
     */
    @SuppressWarnings ( "unchecked" )
    public Completer<String> getCompleter ( OuterWrapper<?> wr ) {
        if ( wr == null ) {
            return new EmptyCompleter();
        }

        OuterWrapper<?> outerWrapper = wr.get("urn:agno3:objects:1.0:fileshare:user:trustLevels"); //$NON-NLS-1$
        if ( outerWrapper == null ) {
            return new EmptyCompleter();
        }

        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {

                List<String> res = new ArrayList<>();

                Set<FileshareUserTrustLevel> ls;
                try {
                    FileshareUserTrustLevelConfig current = (FileshareUserTrustLevelConfig) outerWrapper.getEditor().getCurrent();

                    if ( current == null || current.getTrustLevels().isEmpty() ) {
                        FileshareUserTrustLevelConfig defaults = (FileshareUserTrustLevelConfig) outerWrapper.getEditor().getDefaults();

                        if ( defaults == null || defaults.getTrustLevels().isEmpty() ) {
                            return res;
                        }

                        ls = defaults.getTrustLevels();
                    }
                    else {
                        ls = current.getTrustLevels();
                    }

                }
                catch (
                    ModelObjectNotFoundException |
                    ModelServiceException |
                    GuiWebServiceException |
                    UndeclaredThrowableException e ) {
                    return res;
                }

                String uc = query != null ? query.toUpperCase() : null;

                for ( FileshareUserTrustLevel tl : ls ) {
                    if ( query != null && ( tl.getTrustLevelId() != null && tl.getTrustLevelId().toUpperCase().startsWith(uc) ) ) {
                        res.add(tl.getTrustLevelId());
                    }
                }
                return res;
            }

        };
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, FileshareUserTrustLevelImpl cloned, FileshareUserTrustLevel local,
            FileshareUserTrustLevel def ) {
        cloned.setTrustLevelId(local.getTrustLevelId());
        cloned.setColor(local.getColor());
        cloned.setMatchRoles(new HashSet<>(local.getMatchRoles()));
        cloned.setTitle(local.getTitle());
        cloned.setMessages(new HashMap<>(local.getMessages()));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( FileshareUserTrustLevel obj ) {
        return obj.getTrustLevelId();
    }
}
