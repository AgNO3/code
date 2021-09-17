/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info.internal;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.orchestrator.system.info.SystemInformation;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.SystemInformationProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = SystemInformationProviderImpl.class )
public class SystemInformationProviderImpl {

    private Set<SystemInformationProvider<?>> sysInfoProviders = new HashSet<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindSystemInformationProvider ( SystemInformationProvider<?> provider ) {
        this.sysInfoProviders.add(provider);
    }


    protected synchronized void unbindSystemInformationProvider ( SystemInformationProvider<?> provider ) {
        this.sysInfoProviders.remove(provider);
    }


    /**
     * @return list of system information fragments
     * @throws SystemInformationException
     * 
     */
    public List<SystemInformation> refresh () throws SystemInformationException {
        List<SystemInformation> res = new ArrayList<>();
        for ( SystemInformationProvider<? extends SystemInformation> provider : this.sysInfoProviders ) {
            res.add(provider.getInformation());
        }

        return res;
    }

}
