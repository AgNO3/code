/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit;


import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class PackageId implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8910535535482046278L;

    private String packageName;
    private String packageArch;
    private String packageVersion;
    private String packageRepo;


    /**
     * 
     */
    public PackageId () {}


    /**
     * @param update
     */
    public PackageId ( PackageId update ) {
        this.packageName = update.packageName;
        this.packageArch = update.packageArch;
        this.packageRepo = update.packageRepo;
        this.packageVersion = update.packageVersion;
    }


    /**
     * @return the packageName
     */
    public String getPackageName () {
        return this.packageName;
    }


    /**
     * @param packageName
     *            the packageName to set
     */
    public void setPackageName ( String packageName ) {
        this.packageName = packageName;
    }


    /**
     * @return the packageArch
     */
    public String getPackageArch () {
        return this.packageArch;
    }


    /**
     * @param packageArch
     *            the packageArch to set
     */
    public void setPackageArch ( String packageArch ) {
        this.packageArch = packageArch;
    }


    /**
     * @return the packageRepo
     */
    public String getPackageRepo () {
        return this.packageRepo;
    }


    /**
     * @param packageRepo
     *            the packageRepo to set
     */
    public void setPackageRepo ( String packageRepo ) {
        this.packageRepo = packageRepo;
    }


    /**
     * @return the packageVersion
     */
    public String getPackageVersion () {
        return this.packageVersion;
    }


    /**
     * @param packageVersion
     *            the packageVersion to set
     */
    public void setPackageVersion ( String packageVersion ) {
        this.packageVersion = packageVersion;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s;%s;%s;%s", //$NON-NLS-1$
            this.packageName,
            this.packageVersion,
            this.packageArch != null ? this.packageArch : StringUtils.EMPTY,
            this.packageRepo != null ? this.packageRepo : StringUtils.EMPTY);
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.packageArch == null ) ? 0 : this.packageArch.hashCode() );
        result = prime * result + ( ( this.packageName == null ) ? 0 : this.packageName.hashCode() );
        result = prime * result + ( ( this.packageRepo == null ) ? 0 : this.packageRepo.hashCode() );
        result = prime * result + ( ( this.packageVersion == null ) ? 0 : this.packageVersion.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        PackageId other = (PackageId) obj;
        if ( this.packageArch == null ) {
            if ( other.packageArch != null )
                return false;
        }
        else if ( !this.packageArch.equals(other.packageArch) )
            return false;
        if ( this.packageName == null ) {
            if ( other.packageName != null )
                return false;
        }
        else if ( !this.packageName.equals(other.packageName) )
            return false;
        if ( this.packageRepo == null ) {
            if ( other.packageRepo != null )
                return false;
        }
        else if ( !this.packageRepo.equals(other.packageRepo) )
            return false;
        if ( this.packageVersion == null ) {
            if ( other.packageVersion != null )
                return false;
        }
        else if ( !this.packageVersion.equals(other.packageVersion) )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * 
     * @param s
     * @return a parsed package id
     */
    public static PackageId fromString ( String s ) {
        String[] parts = StringUtils.splitPreserveAllTokens(s, ';');

        if ( parts == null || parts.length != 4 ) {
            throw new IllegalArgumentException("Invalid package id"); //$NON-NLS-1$
        }

        PackageId pid = new PackageId();
        pid.packageName = parts[ 0 ];
        pid.packageVersion = parts[ 1 ];

        if ( !StringUtils.isBlank(parts[ 2 ]) ) {
            pid.packageArch = parts[ 2 ];
        }

        if ( !StringUtils.isBlank(parts[ 3 ]) ) {
            pid.packageRepo = parts[ 3 ];
        }

        return pid;
    }
}
