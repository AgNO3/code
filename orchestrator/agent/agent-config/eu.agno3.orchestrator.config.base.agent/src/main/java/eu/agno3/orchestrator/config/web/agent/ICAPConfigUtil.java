/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web.agent;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import eu.agno3.orchestrator.config.web.ICAPConfiguration;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
public final class ICAPConfigUtil {

    /**
     * 
     */
    private ICAPConfigUtil () {}


    /**
     * @param b
     * @param ctx
     * @param icapInstance
     * @param ic
     * @throws JobBuilderException
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    @SuppressWarnings ( "nls" )
    public static void makeICAPConfiguration ( JobBuilder b, RuntimeConfigContext<?, ?> ctx, String icapInstance, ICAPConfiguration ic )
            throws JobBuilderException, InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        List<String> servers = new ArrayList<>();
        for ( URI uri : ic.getServers() ) {
            if ( "icap".equals(uri.getScheme()) && ic.getSslClientMode() == SSLClientMode.SSL ) {
                throw new JobBuilderException("SSL enabled but URI states icap protocol");
            }
            servers.add(uri.toString());
        }

        PropertyConfigBuilder p = PropertyConfigBuilder.get().p("servers", servers).p("socketTimeout", ic.getSocketTimeout())
                .p("sendICAPSinRequest", ic.getSendICAPSInRequestUri()).p("overrideReqURI", ic.getOverrideRequestURI());

        if ( ic.getSslClientMode() == SSLClientMode.TRY_STARTTLS ) {
            p.p("tryStartTLS", true);
        }
        else if ( ic.getSslClientMode() == SSLClientMode.REQUIRE_STARTTLS ) {
            p.p("requireStartTLS", true);
        }

        String sslInstanceId = "icap-" + icapInstance;
        String subsystem = "icap";
        p.p("TLSContext.target", FilterBuilder.get().eq("instanceId", sslInstanceId).toString());
        SSLConfigUtil.setupSSLClientMapping(b, ctx, sslInstanceId, subsystem, ic.getSslClientConfiguration(), null);
        ctx.factory("icap", icapInstance, p);
    }

}
