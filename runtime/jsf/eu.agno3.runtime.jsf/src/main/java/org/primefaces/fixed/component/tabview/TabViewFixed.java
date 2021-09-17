/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.08.2014 by mbechler
 */
package org.primefaces.fixed.component.tabview;


import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.util.Constants;


/**
 * @author mbechler
 * 
 */
public class TabViewFixed extends TabView {

    private static final String PROCESS_ON_CHANGE = "processOnChange"; //$NON-NLS-1$
    private int oldActiveIndex;


    private boolean alwaysProcessOnChange () {
        String attr = (String) this.getAttributes().get(PROCESS_ON_CHANGE);
        return attr != null && Boolean.parseBoolean(attr);
    }


    @Override
    public void processDecodes ( FacesContext context ) {
        if ( !isRendered() ) {
            return;
        }

        pushComponentToEL(context, null);

        if ( this.isRequestSource(context) && this.alwaysProcessOnChange() ) {
            int i = this.getActiveIndex();
            if ( i >= 0 ) {
                UIComponent openTab = this.getChildren().get(i);
                if ( openTab instanceof Tab ) {
                    openTab.processDecodes(context);
                }
            }
        }
        popComponentFromEL(context);
        super.processDecodes(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.component.tabview.TabView#setActiveIndex(int)
     */
    @Override
    public void setActiveIndex ( int _activeIndex ) {
        if ( this.oldActiveIndex != _activeIndex ) {
            this.oldActiveIndex = this.getActiveIndex();
        }
        else {
            this.oldActiveIndex = -1;
        }
        super.setActiveIndex(_activeIndex);
    }


    @Override
    public void processValidators ( FacesContext context ) {
        pushComponentToEL(context, this);
        try {
            if ( !isRendered() ) {
                return;
            }

            super.processValidators(context);

            if ( !this.isRequestSource(context) || !this.alwaysProcessOnChange() ) {
                return;
            }

            int i = this.oldActiveIndex;
            if ( i >= 0 && i < this.getChildCount() ) {
                Tab t = getActiveTab(context, this.oldActiveIndex);
                if ( t != null ) {
                    t.processValidators(context);
                }
            }
        }
        finally {
            popComponentFromEL(context);
        }
    }


    @Override
    public void processUpdates ( FacesContext context ) {
        pushComponentToEL(context, this);
        try {
            if ( !isRendered() ) {
                return;
            }

            if ( this.isRequestSource(context) && this.alwaysProcessOnChange() ) {
                // if this is a tab change, update the last active tab
                Tab t = getActiveTab(context, this.oldActiveIndex);
                if ( t != null ) {
                    t.processUpdates(context);
                }

            }
            else if ( !isRepeating() && isDynamic() ) {
                // otherwise, update the current tab contents
                Tab t = getActiveTab(context, this.getActiveIndex());
                if ( t != null ) {
                    t.processUpdates(context);
                }
            }
        }
        finally {
            popComponentFromEL(context);
        }

        if ( isRepeating() || !isDynamic() ) {
            super.processUpdates(context);
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#encodeAll(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeAll ( FacesContext context ) throws IOException {
        if ( this.isDynamic() ) {
            Tab activeTab = getActiveTab(context, getActiveIndex());
            for ( UIComponent component : getChildren() ) {
                if ( component instanceof Tab ) {
                    Tab tab = (Tab) component;
                    if ( activeTab == tab ) {
                        tab.setLoaded(true);
                    }
                    else {
                        tab.setLoaded(false);
                    }
                }
            }

        }
        super.encodeAll(context);
    }


    /**
     * @param context
     * @param activeIndex
     * @return
     */
    protected Tab getActiveTab ( FacesContext context, int activeIndex ) {
        int childCount = this.getChildCount();
        if ( activeIndex >= 0 && activeIndex < childCount ) {
            int renderedIndex = 0;
            for ( int idx = 0; idx < childCount; idx++ ) {
                UIComponent tab = this.getChildren().get(idx);
                if ( tab instanceof Tab ) {
                    // active index does not seem to account for unrendered tabs
                    if ( !tab.isRendered() ) {
                        continue;
                    }

                    if ( renderedIndex == activeIndex ) {
                        return (Tab) tab;
                    }

                    renderedIndex++;
                }
            }
        }
        return null;
    }


    private boolean isRequestSource ( FacesContext context ) {
        return this.getClientId(context)
                .equals(context.getExternalContext().getRequestParameterMap().get(Constants.RequestParams.PARTIAL_SOURCE_PARAM));
    }
}
