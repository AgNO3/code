/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import eu.agno3.fileshare.service.UploadStateTracker;


/**
 * @author mbechler
 *
 */
public class FileUploadStateTrackerImpl extends BaseUploadStateTrackerImpl implements UploadStateTracker {

    private Path contextPath;


    /**
     * @param contextPath
     * 
     */
    public FileUploadStateTrackerImpl ( Path contextPath ) {
        this.contextPath = contextPath;
    }


    /**
     * @return the contextPath
     */
    public Path getContextPath () {
        return this.contextPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.UploadStateTracker#isValid()
     */
    @Override
    public boolean isValid () {
        return Files.isDirectory(getContextPath()) && Files.isWritable(getContextPath());
    }


    @Override
    protected boolean checkFlag ( String flag ) {
        return Files.exists(getContextPath().resolve(flag));
    }


    @Override
    protected void setFlag ( String flag ) throws IOException {
        Files.write(getContextPath().resolve(flag), new byte[0], StandardOpenOption.CREATE);
    }


    @Override
    protected void removeFlag ( String flag ) throws IOException {
        Files.deleteIfExists(getContextPath().resolve(flag));
    }

}
