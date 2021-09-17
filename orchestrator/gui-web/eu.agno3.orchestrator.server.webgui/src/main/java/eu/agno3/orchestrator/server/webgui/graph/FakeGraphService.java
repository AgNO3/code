/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.Archive;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.data.DataProcessor;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FakeGraphService {

    private static final Logger log = Logger.getLogger(FakeGraphService.class);


    /**
     * @param selectedObject
     * @return available graph categories
     */
    public List<GraphCategory> getCategories ( StructuralObject selectedObject ) {
        GraphCategory cat = new GraphCategory();
        cat.setTitleMsgId("test"); //$NON-NLS-1$
        cat.setInstances(Arrays.asList("test", "test2")); //$NON-NLS-1$ //$NON-NLS-2$
        cat.setDefault(true);
        return Arrays.asList(cat);
    }


    public GraphData getGraph ( UUID objectId, String inst, DateTime start, DateTime end ) {
        GraphDefinition def = getDefinition(objectId, inst);
        DataProcessor dproc;
        long e = end.getMillis();
        long s = start.getMillis();
        int resolution = (int) ( ( e - s ) / 500 );
        try {
            RrdDb rrd = getRRD(def, objectId, inst);
            // we want approximately 500 data points in the graph
            dproc = getRRDDataPoints(rrd, s, e, resolution);
        }
        catch ( IOException e1 ) {
            log.error("Failed to get RRD data", e1); //$NON-NLS-1$
            return null;
        }

        return GraphData.fromDataProcessor(def, inst, dproc, s, e, resolution);
    }


    public List<GraphData> getGraphs ( UUID objectId, List<String> instances, DateTime start, DateTime end ) {
        List<GraphData> data = new ArrayList<>();
        for ( String inst : instances ) {
            GraphData gd = getGraph(objectId, inst, start, end);
            if ( gd != null ) {
                data.add(gd);
            }
        }
        return data;
    }


    /**
     * @param inst
     * @param objectId
     * @return
     */
    GraphDefinition getDefinition ( UUID objectId, String inst ) {
        GraphDefinition def = new GraphDefinition();
        def.setTitleString("test"); //$NON-NLS-1$
        def.setType(GraphType.TIMESERIES);
        GraphSeriesDefinition seriesDefinition = new GraphSeriesDefinition("test"); //$NON-NLS-1$
        seriesDefinition.setTitleString("foo"); //$NON-NLS-1$
        def.addSeries(seriesDefinition);

        GraphSeriesDefinition seriesDefinition2 = new GraphSeriesDefinition("test2"); //$NON-NLS-1$
        seriesDefinition2.setTitleString("bar"); //$NON-NLS-1$
        def.addSeries(seriesDefinition2);
        return def;
    }


    /**
     * @param objectId
     * @param inst
     * @param start
     * @param resolution
     * @return incremental graph data
     */
    public GraphData getIncremental ( UUID objectId, String inst, DateTime start, int resolution ) {
        GraphDefinition def = getDefinition(objectId, inst);
        DataProcessor dproc;
        long s = start.getMillis();
        long e = System.currentTimeMillis();
        if ( ( e - s ) < ( def.getSampleInterval() * 1000 ) ) {
            return null;
        }
        try {
            RrdDb rrd = getRRD(def, objectId, inst);
            dproc = getRRDDataPoints(rrd, s, e, resolution);
        }
        catch ( IOException e1 ) {
            log.error("Failed to get RRD data", e1); //$NON-NLS-1$
            return null;
        }

        if ( dproc == null ) {
            return null;
        }

        return GraphData.fromDataProcessor(def, inst, dproc, s, e, resolution);
    }


    public List<GraphData> getIncrementals ( UUID objectId, List<String> instances, DateTime start, int resolution ) {
        List<GraphData> data = new ArrayList<>();
        for ( String inst : instances ) {
            GraphData gd = getIncremental(objectId, inst, start, resolution);
            if ( gd != null ) {
                data.add(gd);
            }
        }
        return data;
    }


    private RrdDb getRRD ( GraphDefinition def, UUID objectId, String inst ) throws IOException {
        Path file = Paths.get("/tmp/test.rrd"); //$NON-NLS-1$
        if ( !Files.exists(file) ) {
            DateTime start = DateTime.now().minusDays(1);
            RrdDef rrdDef = createRRDDef(def, start);

            RrdDb db = new RrdDb(rrdDef);

            addFakeData(start, db);
            return db;
        }
        RrdDb rrdDb = new RrdDb(file.toAbsolutePath().toString());
        return rrdDb;
    }


    /**
     * @param start
     * @param db
     * @throws IOException
     */
    void addFakeData ( DateTime start, RrdDb db ) throws IOException {
        Random r = new Random();
        DateTime now = DateTime.now();
        for ( int i = 1; i < 10000; i++ ) {
            if ( i < 500 && i > 400 ) {}
            else {
                long millis = start.plus(i * 60000).getMillis();
                if ( millis >= now.getMillis() ) {
                    break;
                }
                Sample sample = db.createSample(millis);
                sample.setValue("test", i + ( r.nextInt(512) - 256 )); //$NON-NLS-1$
                sample.update();
            }
        }
    }


    /**
     * @param start
     * @param def
     * @return
     */
    RrdDef createRRDDef ( GraphDefinition def, DateTime start ) {
        RrdDef rrdDef = new RrdDef("/tmp/test.rrd"); //$NON-NLS-1$
        rrdDef.setStartTime(start.getMillis());
        rrdDef.setStep(def.getSampleInterval() * 1000);

        for ( String ser : def.getSeries() ) {
            GraphSeriesDefinition sd = def.getSeries(ser);
            rrdDef.addDatasource(
                ser,
                mapType(sd.getType()),
                def.getSampleInterval() * (int) ( 1000 * sd.getHeartbeatFactor() ),
                sd.getMinimumValue(),
                sd.getMaximumValue());
        }

        // keep samples for 24 hours
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.9, 1, 60 * 24);

        // store hourly min/max/average for 1 week
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.3, 60, 24 * 7);
        rrdDef.addArchive(ConsolFun.MIN, 0.3, 60, 24 * 7);
        rrdDef.addArchive(ConsolFun.MAX, 0.3, 60, 24 * 7);
        rrdDef.addArchive(ConsolFun.TOTAL, 0.3, 60, 24 * 7);

        // store daily min/max/average for 180 days
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.1, 7 * 24 * 60, 180);
        rrdDef.addArchive(ConsolFun.MIN, 0.1, 7 * 24 * 60, 180);
        rrdDef.addArchive(ConsolFun.MAX, 0.1, 7 * 24 * 60, 180);
        rrdDef.addArchive(ConsolFun.TOTAL, 0.1, 7 * 24 * 60, 180);

        return rrdDef;
    }


    /**
     * @param type
     * @return
     */
    private static DsType mapType ( SeriesType type ) {
        switch ( type ) {
        case COUNTER:
            return DsType.COUNTER;
        case GAUGE:
            return DsType.GAUGE;
        case ABSOLUTE:
            return DsType.ABSOLUTE;
        case DERIVE:
            return DsType.DERIVE;
        default:
            throw new IllegalArgumentException("Unsupported type " + type); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @param s
     * @return
     * @throws IOException
     */
    DataProcessor getRRDDataPoints ( RrdDb rrd, long start, long end, int resolution ) throws IOException {
        Archive samples = findBestArchive(rrd, ConsolFun.AVERAGE, resolution);
        long dstart = Math.max(start, samples.getStartTime());
        long dend = Math.min(end, samples.getEndTime());

        if ( dstart == dend ) {
            return null;
        }

        long arcStep = samples.getArcStep();
        DataProcessor dproc = new DataProcessor(dstart, dend);
        dproc.setPixelCount(1024);
        dproc.setFetchRequestResolution(arcStep);
        try {
            FetchRequest fr = rrd.createFetchRequest(ConsolFun.AVERAGE, dstart, dend, dproc.getFetchRequestResolution());
            FetchData fetchData = fr.fetchData();
            for ( String dataSource : fetchData.getDsNames() ) {
                dproc.addDatasource(dataSource, fetchData);
            }

            if ( rrd.getArchive(ConsolFun.MAX, samples.getSteps()) != null ) {
                fr = rrd.createFetchRequest(ConsolFun.MAX, dstart, dend, dproc.getFetchRequestResolution());
                fetchData = fr.fetchData();
                for ( String dataSource : fetchData.getDsNames() ) {
                    dproc.addDatasource(dataSource + "#max", dataSource, fetchData); //$NON-NLS-1$
                }
            }

            if ( rrd.getArchive(ConsolFun.MIN, samples.getSteps()) != null ) {
                fr = rrd.createFetchRequest(ConsolFun.MIN, dstart, dend, dproc.getFetchRequestResolution());
                fetchData = fr.fetchData();
                for ( String dataSource : fetchData.getDsNames() ) {
                    dproc.addDatasource(dataSource + "#min", dataSource, fetchData); //$NON-NLS-1$
                }
            }
        }
        finally {
            rrd.close();
        }
        dproc.processData();
        return dproc;
    }


    /**
     * @param rrd
     * @return
     * @throws IOException
     */
    private static Archive findBestArchive ( RrdDb rrd, ConsolFun fun, long resolution ) throws IOException {
        long bestDiff = -1;
        Archive bestMatch = null;
        for ( int i = 0; i < rrd.getArcCount(); i++ ) {
            Archive a = rrd.getArchive(i);
            if ( !a.getConsolFun().equals(fun) ) {
                continue;
            }
            long diff = Math.abs(a.getArcStep() - resolution);

            if ( bestDiff < 0 || diff < bestDiff ) {
                bestDiff = diff;
                bestMatch = a;
            }
        }

        if ( bestMatch == null ) {
            throw new IllegalStateException();
        }
        return bestMatch;
    }

}
