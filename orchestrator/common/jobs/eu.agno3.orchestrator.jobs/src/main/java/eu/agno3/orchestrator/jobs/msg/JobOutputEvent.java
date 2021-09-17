/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.impl.TextMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class JobOutputEvent extends TextMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    /**
     * 
     */
    private static final String JOB_ID_PROP = "jobId"; //$NON-NLS-1$
    private static final String OUTPUT_LEVEL = "level"; //$NON-NLS-1$
    private static final String OUTPUT_POSITION = "position"; //$NON-NLS-1$
    private static final String EOF = "eof"; //$NON-NLS-1$


    /**
     * 
     */
    public JobOutputEvent () {
        super();
    }


    /**
     * 
     * @param l
     * @param levelPosition
     */
    public JobOutputEvent ( JobOutputLevel l, long levelPosition ) {
        super();
        this.getProperties().put(OUTPUT_LEVEL, l.name());
        this.getProperties().put(OUTPUT_POSITION, String.valueOf(levelPosition));
    }


    /**
     * @param jobId
     * @param levelPosition
     * @param origin
     */
    public JobOutputEvent ( UUID jobId, long levelPosition, @NonNull MessageSource origin ) {
        this(jobId, JobOutputLevel.INFO, levelPosition, origin);
    }


    /**
     * @param jobId
     * @param level
     * @param levelPosition
     * @param origin
     */
    public JobOutputEvent ( UUID jobId, JobOutputLevel level, long levelPosition, @NonNull MessageSource origin ) {
        super(origin);
        this.getProperties().put(JOB_ID_PROP, jobId.toString());
        this.getProperties().put(OUTPUT_LEVEL, level.name());
        this.getProperties().put(OUTPUT_POSITION, String.valueOf(levelPosition));
    }


    /**
     * @param jobId
     * @param origin
     */
    public JobOutputEvent ( UUID jobId, @NonNull MessageSource origin ) {
        super(origin);
        this.getProperties().put(JOB_ID_PROP, jobId.toString());
        this.getProperties().put(EOF, true);
    }


    /**
     * @return the job id
     */
    public UUID getJobId () {
        return UUID.fromString((String) this.getProperties().get(JOB_ID_PROP));
    }


    /**
     * @return the output level
     */
    public JobOutputLevel getOutputLevel () {
        String out = (String) this.getProperties().get(OUTPUT_LEVEL);
        if ( out != null ) {
            return JobOutputLevel.valueOf(out);
        }
        return null;
    }


    /**
     * 
     * @return the output position after this output
     */
    public long getOutputPosition () {
        return Long.parseLong((String) this.getProperties().get(OUTPUT_POSITION));
    }


    /**
     * 
     * @return whether this marks the end of output
     */
    public boolean isEof () {
        Boolean eof = (Boolean) this.getProperties().get(EOF);
        return eof != null && eof;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new ServersEventScope());
        return scopes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryMode()
     */
    @Override
    public int getDeliveryMode () {
        return DeliveryMode.NON_PERSISTENT;
    }

}
