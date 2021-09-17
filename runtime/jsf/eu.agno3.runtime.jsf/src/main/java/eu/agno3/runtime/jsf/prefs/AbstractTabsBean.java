/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.prefs;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class AbstractTabsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2398760531207804193L;
    private int activeIndex;
    private List<String> tabIds = new ArrayList<>();


    /**
     * 
     */
    public AbstractTabsBean () {
        super();
    }


    /**
     * @param id
     */
    protected void addTab ( String id ) {
        this.tabIds.add(id);
    }


    /**
     * @return the activeIndex
     */
    public int getActiveIndex () {
        return this.activeIndex;
    }


    /**
     * @param activeIndex
     *            the activeIndex to set
     */
    public void setActiveIndex ( int activeIndex ) {
        this.activeIndex = activeIndex;
    }


    /**
     * @param tab
     */
    public void setTab ( String tab ) {
        if ( tab == null ) {
            return;
        }

        int idx = this.tabIds.indexOf(tab);
        if ( idx >= 0 ) {
            this.setActiveIndex(idx);
        }
    }


    /**
     * @return the tab id
     */
    public String getTab () {

        if ( this.activeIndex >= this.tabIds.size() ) {
            return null;
        }

        return this.tabIds.get(this.activeIndex);
    }

}