/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.test.messages.msg;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
public class TestEvent extends EmptyMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    private static final String BASIC_STRING = "basicString"; //$NON-NLS-1$
    private static final String BASIC_LONG = "basicLong"; //$NON-NLS-1$


    /**
     * 
     */
    public TestEvent () {}


    /**
     * @param origin
     * @param basicString
     * @param basicLong
     */
    public TestEvent ( @NonNull MessageSource origin, String basicString, long basicLong ) {
        super(origin);

        this.getProperties().put(BASIC_STRING, basicString);
        this.getProperties().put(BASIC_LONG, basicLong);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new GlobalEventScope());
        return scopes;
    }


    /**
     * @return the basicLong
     */
    public long getBasicLong () {
        return (long) this.getProperties().get(BASIC_LONG);
    }


    /**
     * @return the basicString
     */
    public String getBasicString () {
        return (String) this.getProperties().get(BASIC_STRING);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("TestEvent (basicString=%s,basicLong=%d)", this.getBasicString(), this.getBasicLong()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object arg0 ) {

        if ( arg0 instanceof TestEvent ) {
            TestEvent other = (TestEvent) arg0;
            return this.getBasicString().equals(other.getBasicString()) && this.getBasicLong() == other.getBasicLong();
        }

        return super.equals(arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return (int) ( this.getBasicLong() + this.getBasicString().hashCode() );
    }
}
