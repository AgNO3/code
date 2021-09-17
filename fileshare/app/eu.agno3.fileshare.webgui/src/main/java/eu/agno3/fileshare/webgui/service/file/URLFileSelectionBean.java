/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.share.ShareListBean;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "urlFileSelectionBean" )
public class URLFileSelectionBean extends AbstractSelectionBean<@Nullable EntityKey, @Nullable VFSEntity, FileshareException> implements
        Serializable, EntityGrantInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -3431032429336927330L;

    private static final Logger log = Logger.getLogger(URLFileSelectionBean.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private ShareListBean shareList;

    private boolean firstGrantsLoaded;
    private List<Grant> firstGrants;
    private boolean numGrantsLoaded;
    private int numGrants;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#parseId(java.lang.String)
     */
    @Override
    protected @Nullable EntityKey parseId ( String id ) {
        return this.fsp.getEntityService().parseEntityKey(id);
    }


    /**
     * @return selected ids
     */
    @Deprecated
    public List<EntityKey> getSelectedIds () {
        return getMultiSelectionIds();
    }


    /**
     * 
     * @return the selected id
     */
    @Deprecated
    public EntityKey getSelectedId () {
        return getSingleSelectionId();
    }


    /**
     * @return the entity selection
     */
    @Deprecated
    public List<VFSEntity> getSelectedEntities () {
        return this.getMultiSelection();
    }


    /**
     * 
     * @return the selected entity
     */
    @Deprecated
    public VFSEntity getSelectedEntity () {
        return this.getSingleSelection();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#handleException(java.lang.Exception)
     */
    @Override
    protected void handleException ( Exception e ) {
        ExceptionHandler.handleException(e);
    }


    @Override
    protected VFSEntity fetchObject ( @Nullable EntityKey selection ) throws FileshareException {
        return this.fsp.getEntityService().getEntity(selection);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#getId(java.lang.Object)
     */
    @Override
    protected EntityKey getId ( @Nullable VFSEntity obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getEntityKey();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.EntityGrantInfo#getFirstGrants()
     */
    @Override
    public List<Grant> getFirstGrants () {
        if ( !this.firstGrantsLoaded ) {
            this.firstGrantsLoaded = true;
            this.firstGrants = this.shareList.getFirstShares(this.getSingleSelection());
        }
        return this.firstGrants;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.EntityGrantInfo#getNumGrants()
     */
    @Override
    public int getNumGrants () {
        if ( !this.numGrantsLoaded ) {
            this.numGrantsLoaded = true;
            this.numGrants = this.shareList.getGrantCount(this.getSingleSelection());
        }
        return this.numGrants;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.EntityGrantInfo#getGrantsExceedingLimit()
     */
    @Override
    public int getGrantsExceedingLimit () {
        return Math.max(0, getNumGrants() - this.shareList.getGrantLimit());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#refreshSelection()
     */
    @Override
    public void refreshSelection () {
        log.debug("refresh selection"); //$NON-NLS-1$
        this.firstGrants = Collections.EMPTY_LIST;
        this.firstGrantsLoaded = false;
        this.numGrants = 0;
        this.numGrantsLoaded = false;
        super.refreshSelection();
    }


    /**
     * 
     */
    @Override
    public void refresh () {
        refreshSelection();
    }
}
