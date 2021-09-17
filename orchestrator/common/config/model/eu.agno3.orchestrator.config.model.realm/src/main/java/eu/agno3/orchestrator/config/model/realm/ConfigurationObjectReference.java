/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:ref" )
public class ConfigurationObjectReference extends AbstractConfigurationObject<ConfigurationObjectReference> {

    private String objectTypeName;

    /**
     * 
     */
    private static final long serialVersionUID = 8761047939420388797L;


    /**
     * 
     * @param obj
     */
    public ConfigurationObjectReference ( ConfigurationObject obj ) {

        if ( obj instanceof ConfigurationObjectReference ) {
            throw new IllegalArgumentException("Creating reference of reference"); //$NON-NLS-1$
        }

        this.setDisplayName(obj.getDisplayName());
        this.setName(obj.getName());
        this.setVersion(obj.getVersion());
        this.setRevision(obj.getRevision());
        this.setId(obj.getId());

        ObjectTypeName annot = obj.getType().getAnnotation(ObjectTypeName.class);

        if ( annot == null ) {
            throw new IllegalArgumentException("Configuration object type missing ObjectTypeName annotation: " + obj.getType().getName()); //$NON-NLS-1$
        }

        this.objectTypeName = annot.value();
    }


    /**
     * 
     */
    public ConfigurationObjectReference () {}


    /**
     * @return the objectTypeName
     */
    public String getObjectTypeName () {
        return this.objectTypeName;
    }


    /**
     * @param objectTypeName
     *            the objectTypeName to set
     */
    public void setObjectTypeName ( String objectTypeName ) {
        this.objectTypeName = objectTypeName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    public @NonNull Class<ConfigurationObjectReference> getType () {
        return ConfigurationObjectReference.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        String dn = "anonymous"; //$NON-NLS-1$

        if ( this.getName() != null ) {
            dn = this.getName();
        }
        else if ( this.getDisplayName() != null ) {
            dn = this.getDisplayName();
        }

        return String.format("Reference %s: %s (%s)", this.getObjectTypeName(), dn, this.getId()); //$NON-NLS-1$
    }

}
