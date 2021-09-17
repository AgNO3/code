/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class ChunkInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3163350398634922282L;
    private long startOffset;
    private int index;
    private long endOffset;


    /**
     * 
     */
    public ChunkInfo () {}


    /**
     * @param i
     * @param start
     * @param end
     */
    public ChunkInfo ( int i, long start, long end ) {
        this.index = i;
        this.startOffset = start;
        this.endOffset = end;
    }


    /**
     * 
     * @return the chunk index
     */
    public int getIndex () {
        return this.index;
    }


    /**
     * 
     * @return chunk start offset
     */
    public long getStartOffset () {
        return this.startOffset;
    }


    /**
     * 
     * @return the chunk end offset
     */
    public long getEndOffset () {
        return this.endOffset;
    }


    /**
     * 
     * @return the chunk length
     */
    public long getLength () {
        return getEndOffset() - getStartOffset();
    }

}
