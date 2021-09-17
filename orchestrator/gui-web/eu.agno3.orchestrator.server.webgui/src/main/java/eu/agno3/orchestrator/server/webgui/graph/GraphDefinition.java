/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class GraphDefinition implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4863093097698775113L;

    private Map<String, GraphSeriesDefinition> series = new LinkedHashMap<>();
    private String titleString;
    private String titleMsgId;
    private String msgBundle;

    private GraphType type;


    /**
     * @return the type
     */
    public GraphType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( GraphType type ) {
        this.type = type;
    }


    /**
     * @return the titleString
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
     * @return the msgBundle
     */
    public String getMsgBundle () {
        return this.msgBundle;
    }


    /**
     * @param msgBundle
     *            the msgBundle to set
     */
    public void setMsgBundle ( String msgBundle ) {
        this.msgBundle = msgBundle;
    }


    /**
     * @param ds
     * @return definition for the series
     */
    public GraphSeriesDefinition getSeries ( String ds ) {
        return this.series.get(ds);
    }


    /**
     * 
     * @param def
     */
    public void addSeries ( GraphSeriesDefinition def ) {
        this.series.put(def.getId(), def);
    }


    /**
     * @return defined series
     */
    public Set<String> getSeries () {
        return this.series.keySet();
    }


    /**
     * @return sample interval
     */
    public int getSampleInterval () {
        return 60;
    }

}
