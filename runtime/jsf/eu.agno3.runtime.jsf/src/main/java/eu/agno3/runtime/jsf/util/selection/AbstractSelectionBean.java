/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.runtime.jsf.util.selection;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author mbechler
 * @param <K>
 * @param <T>
 *            object type
 * @param <TEx>
 *
 */
public abstract class AbstractSelectionBean <@Nullable K, @Nullable T, TEx extends Exception> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6606313200029806289L;
    private String encodedSingleSelection;
    private String encodedMultiSelection;


    protected abstract T fetchObject ( K selection ) throws TEx;


    protected abstract K getId ( T obj );


    protected abstract K parseId ( String id );

    private transient boolean singleSelectionLoaded;
    private transient T cachedSingleSelection;
    private boolean singleFromObject;

    private transient boolean multiSelectionLoaded;
    private transient List<T> cachedMultiSelection;
    private boolean multiFromObject;
    private @Nullable K cachedSingleSelectionId;


    /**
     * 
     */
    public AbstractSelectionBean () {
        super();
    }


    /**
     * @return the encodedSingleSelection
     */
    public String getEncodedSingleSelection () {
        if ( this.singleFromObject ) {
            if ( this.cachedSingleSelection != null ) {
                @Nullable
                K id = this.getId(this.cachedSingleSelection);
                if ( id != null ) {
                    return id.toString();
                }
            }
            return null;
        }
        return this.encodedSingleSelection;
    }


    /**
     * @param encodedSingleSelection
     *            the encodedSingleSelection to set
     */
    public void setEncodedSingleSelection ( String encodedSingleSelection ) {
        this.encodedSingleSelection = encodedSingleSelection;
        this.singleFromObject = false;
        this.singleSelectionLoaded = false;
        this.cachedSingleSelection = null;
    }


    /**
     * @return the encodedMultiSelection
     */
    public String getEncodedMultiSelection () {
        if ( this.multiFromObject ) {
            if ( this.cachedMultiSelection != null ) {
                return encodeMultiIds(this.getIds(this.cachedMultiSelection));
            }
            return null;
        }
        return this.encodedMultiSelection;
    }


    /**
     * @param ids
     * @return
     */
    private String encodeMultiIds ( List<K> ids ) {
        return StringUtils.join(ids, '/');
    }


    /**
     * @return
     */
    private List<K> getIds ( List<T> selection ) {
        List<K> ids = new ArrayList<>();
        for ( T obj : selection ) {
            ids.add(this.getId(obj));
        }
        return ids;
    }


    /**
     * @param encodedMultiSelection
     *            the encodedMultiSelection to set
     */
    public void setEncodedMultiSelection ( String encodedMultiSelection ) {
        this.encodedMultiSelection = encodedMultiSelection;
        this.multiFromObject = false;
        this.multiSelectionLoaded = false;
        this.cachedMultiSelection = null;
    }


    /**
     * @return the selection uuid
     */
    public K getSingleSelectionId () {

        if ( this.singleFromObject ) {
            if ( this.cachedSingleSelection != null ) {
                return getId(this.cachedSingleSelection);
            }
            return null;
        }

        if ( StringUtils.isBlank(this.encodedSingleSelection) ) {
            return null;
        }

        if ( this.cachedSingleSelectionId != null ) {
            return this.cachedSingleSelectionId;
        }
        this.cachedSingleSelectionId = parseId(this.encodedSingleSelection);
        return this.cachedSingleSelectionId;
    }


    /**
     * @return the ids of the selected objects
     */
    public List<K> getMultiSelectionIds () {
        List<K> encodedIds = new ArrayList<>();

        if ( this.multiFromObject ) {
            if ( this.cachedMultiSelection != null ) {
                return getIds(this.cachedMultiSelection);
            }
            return null;
        }

        String[] split = StringUtils.split(this.encodedMultiSelection, '/');
        if ( split == null ) {
            return null;
        }

        for ( String encodedId : split ) {
            encodedIds.add(parseId(encodedId));
        }

        return encodedIds;
    }


    /**
     * @return the selected user
     */
    public T getSingleSelection () {
        K selection = getSingleSelectionId();
        if ( selection == null ) {
            return null;
        }

        if ( this.singleSelectionLoaded ) {
            return this.cachedSingleSelection;
        }

        try {
            this.singleSelectionLoaded = true;
            this.cachedSingleSelection = fetchObject(selection);
            return this.cachedSingleSelection;
        }
        catch ( Exception e ) {
            handleException(e);
            this.cachedSingleSelection = null;
            return null;
        }
    }


    protected abstract void handleException ( Exception e );


    /**
     * @return the entity selection
     */
    public List<T> getMultiSelection () {
        if ( this.multiSelectionLoaded ) {
            return this.cachedMultiSelection;
        }

        this.multiSelectionLoaded = true;
        this.cachedMultiSelection = this.fetchObjects(this.getMultiSelectionIds());
        return this.cachedMultiSelection;
    }


    /**
     * @param multiSelectionIds
     * @return the objects
     * @throws FileshareException
     */
    protected List<T> fetchObjects ( List<K> multiSelectionIds ) {
        if ( multiSelectionIds == null ) {
            return null;
        }
        List<T> res = new ArrayList<>();
        for ( K id : multiSelectionIds ) {
            try {
                res.add(this.fetchObject(id));
            }
            catch ( Exception e ) {
                handleException(e);
            }
        }
        return res;
    }


    /**
     * @param g
     */
    public void setSingleSelection ( T g ) {
        this.cachedSingleSelection = g;
        this.cachedSingleSelectionId = null;
        this.singleSelectionLoaded = true;
        this.singleFromObject = true;
    }


    /**
     * @param objs
     */
    public void setMultiSelection ( List<T> objs ) {
        this.cachedMultiSelection = objs;
        this.multiSelectionLoaded = true;
        this.multiFromObject = true;
    }


    /**
     * @param objs
     */
    public void setMultiSelection ( T[] objs ) {
        this.setMultiSelection(Arrays.asList(objs));
    }


    /**
     * 
     */
    public void refreshSelection () {
        T singleSelection = this.getSingleSelection();

        if ( singleSelection == null ) {
            return;
        }

        @Nullable
        K id = getId(singleSelection);
        if ( id != null ) {
            this.encodedSingleSelection = id.toString();
        }
        else {
            this.encodedSingleSelection = StringUtils.EMPTY;
        }
        this.singleSelectionLoaded = false;
        this.cachedSingleSelection = null;
    }
}