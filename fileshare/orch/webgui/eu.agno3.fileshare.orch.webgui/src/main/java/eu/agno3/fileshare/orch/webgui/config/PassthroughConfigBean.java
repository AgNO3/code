/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.fileshare.orch.common.config.AbstractFilesharePassthroughGroupImpl;
import eu.agno3.fileshare.orch.common.config.FilesharePassthroughGroup;
import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fs_passhroughConfigBean" )
public class PassthroughConfigBean
        extends AbstractBaseConfigObjectBean<FilesharePassthroughGroup, AbstractFilesharePassthroughGroupImpl<? extends FilesharePassthroughGroup>> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return FileshareConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<FilesharePassthroughGroup> getObjectType () {
        return FilesharePassthroughGroup.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( FilesharePassthroughGroup obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getGroupName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( AbstractFilesharePassthroughGroupImpl<? extends FilesharePassthroughGroup> cloned,
            FilesharePassthroughGroup obj ) {
        cloned.setGroupName(obj.getGroupName());
        cloned.doClone(obj);
    }


    /**
     * @return comparator
     */
    public Comparator<FilesharePassthroughGroup> getComparator () {
        return new PasstroughGroupComparator();
    }

}
