/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.usage;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractUsageDataModelBean <T extends Serializable> implements Serializable {

    private static final Logger log = Logger.getLogger(AbstractUsageDataModelBean.class);
    private static final long serialVersionUID = -9100600158495756681L;
    private ServerServiceProvider ssp;
    private ConfigurationObject object;
    private List<T> model;
    private String title;


    public AbstractUsageDataModelBean ( String title, ServerServiceProvider ssp, ConfigurationObject object ) {
        this.title = title;
        this.ssp = ssp;
        this.object = object;
    }


    /**
     * @return the ssp
     */
    public ServerServiceProvider getSsp () {
        return this.ssp;
    }


    /**
     * @return the object
     */
    public ConfigurationObject getObject () {
        return this.object;
    }


    /**
     * @return the model
     * @throws GuiWebServiceException
     * @throws AbstractModelException
     */
    public List<T> getModel () throws GuiWebServiceException, AbstractModelException {
        if ( this.model == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Loading model for " + this.title); //$NON-NLS-1$
            }
            this.model = new ArrayList<>(this.createModel(this.object));
            Collections.sort(this.model, makeDisplayComparator());
        }
        return this.model;
    }


    protected abstract Comparator<T> makeDisplayComparator ();


    public String getTitle () {
        return this.title;
    }


    /**
     * @param obj
     * @return
     * @throws GuiWebServiceException
     */
    protected abstract Set<T> createModel ( ConfigurationObject obj ) throws GuiWebServiceException, AbstractModelException;


    /**
     * 
     */
    public void refresh () {
        this.model = null;
    }
}
