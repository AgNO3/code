/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit.internal;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class APTCCRepoId {

    private static final Logger log = Logger.getLogger(APTCCRepoId.class);

    private String sourceFile;
    private String type;
    private String uri;
    private String dist;
    private String[] sections;


    /**
     * @return the sourceFile
     */
    public String getSourceFile () {
        return this.sourceFile;
    }


    /**
     * @return the arch
     */
    public String getType () {
        return this.type;
    }


    /**
     * @return the uri
     */
    public String getUri () {
        return this.uri;
    }


    /**
     * @return the dist
     */
    public String getDist () {
        return this.dist;
    }


    /**
     * @return the sections
     */
    public String[] getSections () {
        return this.sections;
    }


    /**
     * @param repoId
     * @return parsed repo id
     */
    public static APTCCRepoId fromString ( String repoId ) {
        APTCCRepoId parsed = new APTCCRepoId();

        String[] split = StringUtils.splitPreserveAllTokens(repoId, ' ');

        if ( split == null || split.length < 4 ) {
            log.warn("Unsupported repo id " + repoId); //$NON-NLS-1$
            return null;
        }

        String srcAndArch = split[ 0 ];
        int sep = srcAndArch.indexOf(':');
        parsed.sourceFile = srcAndArch.substring(0, sep);
        parsed.type = srcAndArch.substring(sep + 1);

        parsed.uri = split[ 1 ];
        parsed.dist = split[ 2 ];

        parsed.sections = new String[split.length - 3];
        System.arraycopy(split, 3, parsed.sections, 0, split.length - 3);
        return parsed;
    }
}
