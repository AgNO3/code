/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryReference {

    private String name;
    private String hint;
    private String type;


    /**
     * @param type
     * @param name
     * 
     */
    public ResourceLibraryReference ( String type, String name ) {
        this(type, name, null);
    }


    /**
     * @param type
     * @param name
     * @param hint
     */
    public ResourceLibraryReference ( String type, String name, String hint ) {
        this.type = type;
        this.name = name;
        this.hint = hint;
    }


    /**
     * @return the name
     */
    public String getName () {
        return this.name;
    }


    /**
     * @return the type
     */
    public String getType () {
        return this.type;
    }


    /**
     * @return the hint
     */
    public String getHint () {
        return this.hint;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s/%s%s", //$NON-NLS-1$
            this.type,
            this.name,
            this.hint != null ? "#" + this.hint : StringUtils.EMPTY); //$NON-NLS-1$
    }
}
