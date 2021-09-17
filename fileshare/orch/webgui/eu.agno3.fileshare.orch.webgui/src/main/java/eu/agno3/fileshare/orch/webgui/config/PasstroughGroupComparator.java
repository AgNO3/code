/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

import javax.faces.context.FacesContext;

import eu.agno3.fileshare.orch.common.config.FilesharePassthroughGroup;


/**
 * @author mbechler
 *
 */
public class PasstroughGroupComparator implements Serializable, Comparator<FilesharePassthroughGroup> {

    /**
     * 
     */
    private static final long serialVersionUID = -6307075342806605056L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( FilesharePassthroughGroup o1, FilesharePassthroughGroup o2 ) {
        return Objects
                .compare(o1.getGroupName(), o2.getGroupName(), Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()));
    }

}
