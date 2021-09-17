/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml.test.messages.msg;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class TestEvent extends XmlMarshallableMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    private String basicString;
    private long basicLong;


    /**
     * @param s
     * @param basicString
     * @param basicLong
     */
    public TestEvent ( @NonNull MessageSource s, String basicString, long basicLong ) {
        super(s);
        this.basicString = basicString;
        this.basicLong = basicLong;
    }


    /**
     * 
     */
    public TestEvent () {}


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
        return this.basicLong;
    }


    /**
     * @return the basicString
     */
    public String getBasicString () {
        return this.basicString;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("TestEvent (basicString=%s,basicLong=%d)", this.basicString, this.basicLong); //$NON-NLS-1$
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
            return this.basicString.equals(other.basicString) && this.basicLong == other.basicLong;
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
        return (int) ( this.basicLong + this.basicString.hashCode() );
    }
}
