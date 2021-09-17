/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.dns;


/**
 * @author mbechler
 *
 */
public class SRVEntry {

    private int priortity;
    private int weight;

    private String name;
    private int port;
    private int ttl;


    /**
     * @param name
     * @param priority
     * @param weight
     * @param port
     * @param ttl
     * 
     */
    public SRVEntry ( String name, int priority, int weight, int port, int ttl ) {
        this.name = name;

        if ( this.name.charAt(this.name.length() - 1) == '.' ) {
            this.name = this.name.substring(0, this.name.length() - 1);
        }

        this.priortity = priority;
        this.weight = weight;
        this.port = port;
        this.ttl = ttl;
    }


    /**
     * @return the priortity
     */
    public int getPriority () {
        return this.priortity;
    }


    /**
     * @return the weight
     */
    public int getWeight () {
        return this.weight;
    }


    /**
     * @return the name
     */
    public String getName () {
        return this.name;
    }


    /**
     * @return the port
     */
    public int getPort () {
        return this.port;
    }


    /**
     * @return the record TTL
     */
    public int getTTL () {
        return this.ttl;
    }
}
