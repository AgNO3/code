/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.rrd4j.data.Aggregates;
import org.rrd4j.data.DataProcessor;


/**
 * @author mbechler
 *
 */
public class SeriesData {

    private double[] lowerBand;
    private double[] values;
    private double[] upperBand;
    private double minimum;
    private double maximum;
    private double average;
    private double total;


    /**
     * 
     */
    public SeriesData () {}


    public double getMinimum () {
        return this.minimum;
    }


    public double getMaximum () {
        return this.maximum;
    }


    public double getAverage () {
        return this.average;
    }


    public double getTotal () {
        return this.total;
    }


    /**
     * @return values in this series
     */
    public double[] getValues () {
        return this.values;
    }


    /**
     * @return lower error band
     */
    public double[] getLowerBand () {
        return this.lowerBand;
    }


    /**
     * @return upper error band
     */
    public double[] getUpperBand () {
        return this.upperBand;
    }


    static SeriesData fromDataProcessor ( DataProcessor proc, String ds ) {
        SeriesData seriesData = new SeriesData();
        Set<String> sourceNames = new HashSet<>(Arrays.asList(proc.getSourceNames()));

        if ( !sourceNames.contains(ds) ) {
            return seriesData;
        }

        seriesData.values = proc.getValues(ds);

        String minSrc = ds + "#min"; //$NON-NLS-1$
        String maxSrc = ds + "#max"; //$NON-NLS-1$
        if ( sourceNames.contains(minSrc) && sourceNames.contains(maxSrc) ) {
            seriesData.lowerBand = proc.getValues(minSrc);
            seriesData.upperBand = proc.getValues(maxSrc);
        }

        @SuppressWarnings ( "deprecation" )
        Aggregates aggregates = proc.getAggregates(ds);
        seriesData.average = aggregates.getAverage();
        seriesData.total = aggregates.getTotal();
        seriesData.minimum = aggregates.getMin();
        seriesData.maximum = aggregates.getMax();
        return seriesData;
    }
}
