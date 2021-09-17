/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.targets;


import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.agno3.orchestrator.jobs.JobTarget;


/**
 * @author mbechler
 * 
 */
public class TargetXmlAdapter extends XmlAdapter<String, JobTarget> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public JobTarget unmarshal ( String v ) {
        if ( v == null ) {
            return null;
        }

        if ( AnyServerTarget.SERVERS.equals(v) ) {
            return new AnyServerTarget();
        }
        else if ( v.startsWith(ServerTarget.SERVER_PREFIX) ) {
            return makeServerTarget(v);
        }
        else if ( v.startsWith(AgentTarget.AGENT_PREFIX) ) {
            return makeAgentTarget(v);
        }

        throw new IllegalArgumentException("Unknown target " + v); //$NON-NLS-1$
    }


    /**
     * @param v
     * @return
     */
    private static JobTarget makeAgentTarget ( String v ) {
        UUID fromString = UUID.fromString(v.substring(AgentTarget.AGENT_PREFIX.length()));

        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }

        return new AgentTarget(fromString);
    }


    /**
     * @param v
     * @return
     */
    private static JobTarget makeServerTarget ( String v ) {
        UUID fromString = UUID.fromString(v.substring(ServerTarget.SERVER_PREFIX.length()));

        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }

        return new ServerTarget(fromString);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( JobTarget v ) {
        if ( v == null ) {
            return null;
        }

        return v.toString();
    }

}
