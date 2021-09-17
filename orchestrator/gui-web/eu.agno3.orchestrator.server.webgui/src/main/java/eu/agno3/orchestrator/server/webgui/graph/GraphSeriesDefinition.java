/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class GraphSeriesDefinition implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3219590630362849982L;

    private String titleString;
    private String titleMsgId;

    private String seriesId;

    private SeriesType type;


    /**
     * 
     */
    public GraphSeriesDefinition () {}


    /**
     * @param id
     */
    public GraphSeriesDefinition ( String id ) {
        this.seriesId = id;
    }


    /**
     * @return the serires id
     */
    public String getId () {
        return this.seriesId;
    }


    /**
     * @return the type
     */
    public SeriesType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( SeriesType type ) {
        this.type = type;
    }


    /**
     * @return the series title
     */
    public String getTitleString () {
        return this.titleString;
    }


    /**
     * @param titleString
     *            the titleString to set
     */
    public void setTitleString ( String titleString ) {
        this.titleString = titleString;
    }


    /**
     * @return the titleMsgId
     */
    public String getTitleMsgId () {
        return this.titleMsgId;
    }


    /**
     * @param titleMsgId
     *            the titleMsgId to set
     */
    public void setTitleMsgId ( String titleMsgId ) {
        this.titleMsgId = titleMsgId;
    }


    /**
     * @return minimum allowable value
     */
    public double getMinimumValue () {
        return Double.NaN;
    }


    /**
     * @return maximum allowable value
     */
    public double getMaximumValue () {
        return Double.NaN;
    }


    /**
     * @return heartbeat scaling
     */
    public float getHeartbeatFactor () {
        return 1.5f;
    }

}
