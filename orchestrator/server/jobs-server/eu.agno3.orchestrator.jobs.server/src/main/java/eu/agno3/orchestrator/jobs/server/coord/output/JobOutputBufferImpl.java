/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.output;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.coord.db.JobOutputSegment;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;
import eu.agno3.orchestrator.jobs.server.JobOutputBuffer;


/**
 * @author mbechler
 *
 */
public class JobOutputBufferImpl implements JobOutputBuffer {

    private static final Logger log = Logger.getLogger(JobOutputBufferImpl.class);
    private static final int MAX_SEGMENT_SIZE = 65535;
    private final UUID jobId;
    private final Queue<JobOutputSegment> segments = new ConcurrentLinkedQueue<>();
    private final Map<JobOutputLevel, Long> currentOffsets = new EnumMap<>(JobOutputLevel.class);
    private long currentCombinedOffset = 0;
    private boolean eof;


    /**
     * @param jobId
     * 
     */
    public JobOutputBufferImpl ( UUID jobId ) {
        this.jobId = jobId;
        for ( JobOutputLevel l : JobOutputLevel.values() ) {
            this.currentOffsets.put(l, 0L);
        }
    }


    /**
     * @param jobId
     * @param segments
     */
    public JobOutputBufferImpl ( UUID jobId, List<JobOutputSegment> segments ) {
        this(jobId);

        for ( JobOutputSegment seg : segments ) {
            this.segments.add(seg);
            int length = seg.getContent().length();
            this.currentOffsets.put(seg.getLevel(), seg.getOffset() + length);
            this.currentCombinedOffset += length;
            this.eof |= seg.getEof();
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.JobOutputBuffer#isEof()
     */
    @Override
    public boolean isEof () {
        return this.eof;
    }


    /**
     * 
     */
    public void markEof () {
        this.eof = true;
    }


    protected List<JobOutputSegment> getSegments () {
        return new ArrayList<>(this.segments);
    }


    protected Map<JobOutputLevel, Long> getOffsets () {
        return this.currentOffsets;
    }


    /**
     * @param l
     * @return the current output position
     */
    public long getLevelOffset ( JobOutputLevel l ) {
        Long off = getOffsets().get(l);
        if ( off == null ) {
            return 0;
        }
        return off;
    }


    /**
     * @param text
     * @param outputLevel
     */
    public synchronized void append ( String text, JobOutputLevel outputLevel ) {
        if ( text == null ) {
            return;
        }

        long currentOffset = this.currentOffsets.get(outputLevel);
        long length = text.length();
        if ( length > MAX_SEGMENT_SIZE ) {
            for ( int off = 0; off < length; off += MAX_SEGMENT_SIZE ) {
                int end = (int) Math.min(off + MAX_SEGMENT_SIZE, length);
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Generating segment from %d to %d", off, end)); //$NON-NLS-1$
                }
                this.segments.add(
                    new JobOutputSegment(this.jobId, currentOffset + off, this.currentCombinedOffset + off, outputLevel, text.substring(off, end)));
            }
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding %s segment at combined offset %d", outputLevel, this.currentCombinedOffset)); //$NON-NLS-1$
            }
            this.segments.add(new JobOutputSegment(this.jobId, currentOffset, this.currentCombinedOffset, outputLevel, text));
        }

        if ( log.isDebugEnabled() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Adjusting offset for %s %s from %d to %d", //$NON-NLS-1$
                    this.jobId,
                    outputLevel,
                    currentOffset,
                    currentOffset + length));
            }
        }
        this.currentOffsets.put(outputLevel, currentOffset + length);
        this.currentCombinedOffset += length;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.JobOutputBuffer#getCombinedOutput()
     */
    @Override
    public String getCombinedOutput () {
        StringBuilder sb = new StringBuilder();
        for ( JobOutputSegment seg : this.segments ) {
            sb.append(seg.getContent());
        }
        return sb.toString();
    }


    @Override
    public String getCombinedOutput ( long offset ) {

        if ( offset > this.currentCombinedOffset ) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        long combinedOffset = 0;

        for ( JobOutputSegment seg : this.segments ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "Segment @ %d combined %d level %s length %d (combined offset: %d)", //$NON-NLS-1$
                    seg.getOffset(),
                    seg.getCombinedOffset(),
                    seg.getLevel(),
                    seg.getContent().length(),
                    combinedOffset));
            }
            int length = seg.getContent().length();
            // end of current segment is before offset, skip
            if ( combinedOffset + length <= offset ) {
                log.debug("Skip"); //$NON-NLS-1$
                combinedOffset += length;
                continue;
            }

            // offset is after segment start and before segment end
            if ( offset > combinedOffset && offset < combinedOffset + length ) {
                int offInto = (int) ( offset - combinedOffset );
                if ( log.isDebugEnabled() ) {
                    log.debug("Append with offset " + offInto); //$NON-NLS-1$
                }
                sb.append(seg.getContent().substring(offInto));
            }
            else {
                log.debug("Append fully"); //$NON-NLS-1$
                sb.append(seg.getContent());
            }
            combinedOffset += length;
        }
        return sb.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.JobOutputBuffer#getLevelOutput(eu.agno3.orchestrator.jobs.msg.JobOutputLevel)
     */
    @Override
    public String getLevelOutput ( JobOutputLevel l ) {
        StringBuilder sb = new StringBuilder();
        for ( JobOutputSegment seg : this.segments ) {
            if ( l != seg.getLevel() ) {
                continue;
            }
            sb.append(seg.getContent());
        }
        return sb.toString();
    }


    @Override
    public String getLevelOutput ( JobOutputLevel l, long offset ) {

        if ( offset > this.currentOffsets.get(l) ) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for ( JobOutputSegment seg : this.segments ) {
            if ( l != seg.getLevel() ) {
                continue;
            }
            int length = seg.getContent().length();
            // end of current segment is before offset, skip
            if ( seg.getOffset() + length <= offset ) {
                continue;
            }

            // offset is after segment start and before segment end
            if ( offset > seg.getOffset() && offset < seg.getOffset() + length ) {
                sb.append(seg.getContent().substring((int) ( offset - seg.getOffset() )));
            }
            else {
                sb.append(seg.getContent());
            }
        }
        return sb.toString();
    }

}
