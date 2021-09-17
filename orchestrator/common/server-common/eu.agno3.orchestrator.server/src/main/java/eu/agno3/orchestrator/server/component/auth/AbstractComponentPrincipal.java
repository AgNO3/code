/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.auth;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 *
 */
public abstract class AbstractComponentPrincipal implements ComponentPrincipal {

    private String userPrefix;

    @NonNull
    private UUID componentId;


    protected AbstractComponentPrincipal ( String userPrefix, @NonNull UUID componentId ) {
        this.userPrefix = userPrefix;
        this.componentId = componentId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentPrincipal#getType()
     */
    @Override
    public Class<? extends ComponentPrincipal> getType () {
        return this.getClass();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName () {
        return this.userPrefix.concat(this.getComponentId().toString());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentPrincipal#getComponentId()
     */
    @Override
    public @NonNull UUID getComponentId () {
        return this.componentId;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.componentId.hashCode();
        result = prime * result + ( ( this.userPrefix == null ) ? 0 : this.userPrefix.hashCode() );
        return result;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s %s (%s)", this.getType().getSimpleName(), this.getComponentId().toString(), this.getName()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof ComponentPrincipal ) {
            ComponentPrincipal componentPrincipal = (ComponentPrincipal) obj;
            return componentPrincipal.getType().equals(this.getType()) && componentPrincipal.getComponentId().equals(this.getComponentId());
        }

        return false;
    }
}