/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.rrd4j.data.DataProcessor;


/**
 * @author mbechler
 *
 */
public class GraphData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4271434734673815506L;

    private GraphDefinition definition;

    private Map<String, SeriesData> series = new HashMap<>();

    private String instanceId;

    private long[] timestamps;
    private long start;
    private long end;
    private long dataEnd;

    private int resolution;


    /**
     * @return the instanceId
     */
    public String getInstanceId () {
        return this.instanceId;
    }


    /**
     * @param instanceId
     *            the instanceId to set
     */
    public void setInstanceId ( String instanceId ) {
        this.instanceId = instanceId;
    }


    /**
     * @return the resolution
     */
    public int getResolution () {
        return this.resolution;
    }


    /**
     * @param resolution
     *            the resolution to set
     */
    public void setResolution ( int resolution ) {
        this.resolution = resolution;
    }


    /**
     * @return the definition
     */
    public GraphDefinition getDefinition () {
        return this.definition;
    }


    /**
     * @param definition
     *            the definition to set
     */
    public void setDefinition ( GraphDefinition definition ) {
        this.definition = definition;
    }


    /**
     * @param ds
     * @return data for series
     */
    public SeriesData getSeries ( String ds ) {
        return this.series.get(ds);
    }


    /**
     * @return timestamps in this data series
     */
    public long[] getTimestamps () {
        return this.timestamps;
    }


    /**
     * @return the start
     */
    public long getStart () {
        return this.start;
    }


    /**
     * @return the time of the last data sample
     */
    public long getDataEnd () {
        return this.dataEnd;
    }


    /**
     * @return the end
     */
    public long getEnd () {
        return this.end;
    }


    /**
     * @param def
     * @param instanceId
     * @param dproc
     * @param start
     * @param end
     * @param resolution
     * @return graph data
     */
    public static GraphData fromDataProcessor ( GraphDefinition def, String instanceId, DataProcessor dproc, long start, long end, int resolution ) {
        GraphData d = new GraphData();
        d.instanceId = instanceId;
        d.resolution = resolution;
        d.definition = def;
        d.end = end;
        d.start = start;
        if ( dproc != null ) {
            d.timestamps = dproc.getTimestamps();
            d.dataEnd = dproc.getEndingTimestamp();
            for ( String ser : def.getSeries() ) {
                d.series.put(ser, SeriesData.fromDataProcessor(dproc, ser));
            }
        }
        return d;
    }

}
