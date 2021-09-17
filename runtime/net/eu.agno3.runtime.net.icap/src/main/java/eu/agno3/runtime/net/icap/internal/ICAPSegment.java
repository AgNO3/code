/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


class ICAPSegment {

    private final String type;
    private final long offset;
    private final long length;


    /**
     * @param type
     * @param offset
     * @param length
     * 
     */
    public ICAPSegment ( String type, long offset, long length ) {
        this.type = type;
        this.offset = offset;
        this.length = length;
    }


    /**
     * @return the type
     */
    public String getType () {
        return this.type;
    }


    /**
     * @return the offset
     */
    public long getOffset () {
        return this.offset;
    }


    /**
     * @return the length
     */
    public long getLength () {
        return this.length;
    }
}