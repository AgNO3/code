/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.defaults;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
@Named ( "inlineEditContext" )
@ViewScoped
public class InlineEditContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7468195302476799007L;

    private static final Logger log = Logger.getLogger(InlineEditContextBean.class);

    private ConfigurationObject oldVal;
    private ConfigurationObject newVal;


    /**
     * @return the oldVal
     */
    public ConfigurationObject getOldVal () {
        return this.oldVal;
    }


    /**
     * @param oldVal
     *            the oldVal to set
     */
    public void setOldVal ( ConfigurationObject oldVal ) {
        this.oldVal = oldVal;
    }


    /**
     * @return the newVal
     */
    public ConfigurationObject getNewVal () {
        return this.newVal;
    }


    /**
     * @param newVal
     *            the newVal to set
     */
    public void setNewVal ( ConfigurationObject newVal ) {
        this.newVal = newVal;
    }


    /**
     * 
     * @param cur
     * @return the context adapted to a new edit transaction (oldVal == cur)
     */
    public InlineEditContextBean get ( ConfigurationObject cur ) {

        if ( log.isDebugEnabled() ) {
            log.debug("Get context for " + cur); //$NON-NLS-1$
        }

        if ( cur == null ) {
            return null;
        }

        if ( !cur.equals(this.oldVal) ) {
            this.oldVal = cur;
            this.newVal = cur;
        }

        return this;
    }

}
