/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.coord.impl;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.jobs.coord.QueueFactory;
import eu.agno3.orchestrator.jobs.coord.internal.queue.QueueFactoryImpl;


/**
 * @author mbechler
 * 
 */
@Component ( service = QueueFactory.class )
public class AgentQueueFactory extends QueueFactoryImpl {

}
