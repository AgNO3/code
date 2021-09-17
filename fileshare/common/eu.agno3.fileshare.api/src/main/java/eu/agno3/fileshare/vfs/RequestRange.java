/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.vfs;




/**
 * @author mbechler
 *
 */
public class RequestRange {

    private long start;
    private long end;


    /**
     * @param start
     * @param end
     */
    public RequestRange ( long start, long end ) {
        this.start = start;
        this.end = end;
    }


    /**
     * @return the start
     */
    public long getStart () {
        return this.start;
    }


    /**
     * @return the end
     */
    public long getEnd () {
        return this.end;
    }

}
