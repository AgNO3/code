/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.HrefProperty;


/**
 * @author mbechler
 *
 */
public class ResolvedHrefProperty extends HrefProperty implements WriteReplaceProperty {

    private boolean[] collection;


    /**
     * @param name
     * @param value
     * @param collection
     * @param isInvisibleInAllprop
     */
    public ResolvedHrefProperty ( DavPropertyName name, String value, boolean collection, boolean isInvisibleInAllprop ) {
        this(name, new String[] {
            value
        }, new boolean[] {
            collection
        }, isInvisibleInAllprop);
    }


    /**
     * @param name
     * @param value
     * @param collection
     * @param isInvisibleInAllprop
     */
    public ResolvedHrefProperty ( DavPropertyName name, String[] value, boolean[] collection, boolean isInvisibleInAllprop ) {
        super(name, value, isInvisibleInAllprop);
        if ( value.length != collection.length ) {
            throw new IllegalArgumentException();
        }
        this.collection = collection;
    }


    /**
     * @return the collection
     */
    public boolean[] getIsCollection () {
        return this.collection;
    }


    @Override
    public DavProperty<?> writeReplaceProperty ( DavResourceLocator locator, DavLocatorFactory locatorFactory ) {
        String[] resolved = new String[this.getHrefs().size()];
        for ( int j = 0; j < this.getHrefs().size(); j++ ) {
            String href = this.getHrefs().get(j);
            resolved[ j ] = locatorFactory.createResourceLocator(locator.getPrefix(), locator.getWorkspacePath(), href)
                    .getHref(this.getIsCollection()[ j ]);
        }
        return new HrefProperty(this.getName(), resolved, this.isInvisibleInAllprop());
    }

}
