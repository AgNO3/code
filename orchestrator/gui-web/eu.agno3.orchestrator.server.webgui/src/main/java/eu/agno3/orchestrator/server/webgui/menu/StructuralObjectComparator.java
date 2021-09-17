/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

import javax.faces.context.FacesContext;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * 
 */
public class StructuralObjectComparator implements Comparator<StructuralObject>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4249913434399152642L;


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( StructuralObject a, StructuralObject b ) {
        int res = a.getType().compareTo(b.getType());
        if ( res != 0 ) {
            return res;
        }

        if ( a instanceof ServiceStructuralObject && b instanceof ServiceStructuralObject ) {
            res = compareService((ServiceStructuralObject) a, (ServiceStructuralObject) b);
        }
        if ( res != 0 ) {
            return res;
        }

        return Objects.compare(
            a.getDisplayName(),
            b.getDisplayName(),
            Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()));

    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int compareService ( ServiceStructuralObject a, ServiceStructuralObject b ) {
        if ( HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(a.getServiceType())
                && HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(b.getServiceType()) ) {
            return 0;
        }
        else if ( HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(a.getServiceType()) ) {
            return -1;
        }
        else if ( HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(b.getServiceType()) ) {
            return 1;
        }
        return a.getServiceType().compareTo(b.getServiceType());
    }

}
