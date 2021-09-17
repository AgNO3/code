/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2015 by mbechler
 */
package eu.agno3.runtime.util.sid;


import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class SID implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5179753540271724410L;

    private int revision;
    private long idAuthority;
    private int[] subauthorities;


    /**
     * 
     */
    public SID () {
        this.subauthorities = new int[0];
    }


    /**
     * 
     * @param parent
     * @param rid
     */
    public SID ( SID parent, int rid ) {
        this.revision = parent.revision;
        this.idAuthority = parent.idAuthority;
        this.subauthorities = new int[parent.subauthorities.length + 1];
        System.arraycopy(parent.subauthorities, 0, this.subauthorities, 0, parent.subauthorities.length);
        this.subauthorities[ parent.subauthorities.length ] = rid;
    }


    /**
     * @param revision
     * @param idAuthority
     * @param subauthorities
     */
    public SID ( int revision, long idAuthority, int[] subauthorities ) {
        super();
        this.revision = revision;
        this.idAuthority = idAuthority;
        if ( subauthorities != null ) {
            this.subauthorities = Arrays.copyOf(subauthorities, subauthorities.length);
        }
    }


    /**
     * @return the parent SID
     */
    public SID getParent () {
        if ( this.subauthorities.length == 1 ) {
            throw new IllegalArgumentException();
        }

        int[] parentSubauthorities = new int[this.subauthorities.length - 1];
        System.arraycopy(this.subauthorities, 0, parentSubauthorities, 0, this.subauthorities.length - 1);
        return new SID(this.revision, this.idAuthority, parentSubauthorities);
    }


    /**
     * @return the RID
     */
    public int getRid () {
        return this.subauthorities[ this.subauthorities.length - 1 ];
    }


    /**
     * @return the revision
     */
    public int getRevision () {
        return this.revision;
    }


    /**
     * @return the idAuthority
     */
    public long getIdAuthority () {
        return this.idAuthority;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("S-"); //$NON-NLS-1$
        sb.append(this.revision);
        sb.append('-');
        sb.append(this.idAuthority);
        sb.append('-');
        boolean first = true;
        for ( long subauth : this.subauthorities ) {
            if ( !first ) {
                sb.append('-');
            }
            else {
                first = false;
            }
            sb.append(subauth & 0xFFFFFFFFL);
        }
        return sb.toString();
    }


    /**
     * 
     * @return the binary reprentation of this SID
     */
    public byte[] toBinary () {
        byte[] res = new byte[8 + 4 * this.subauthorities.length];
        res[ 0 ] = (byte) this.revision;
        res[ 1 ] = (byte) this.subauthorities.length;
        res[ 2 ] = (byte) ( this.idAuthority & ( (long) 0xFF << 40L ) );
        res[ 3 ] = (byte) ( this.idAuthority & ( (long) 0xFF << 32L ) );
        res[ 4 ] = (byte) ( this.idAuthority & ( (long) 0xFF << 24L ) );
        res[ 5 ] = (byte) ( this.idAuthority & ( (long) 0xFF << 16L ) );
        res[ 6 ] = (byte) ( this.idAuthority & ( (long) 0xFF << 8L ) );
        res[ 7 ] = (byte) ( this.idAuthority & 0xFF );
        for ( int i = 0; i < this.subauthorities.length; i++ ) {
            uint32le(this.subauthorities[ i ], res, 8 + 4 * i);
        }
        return res;
    }


    /**
     * @param i
     * @param res
     * @param j
     */
    private static void uint32le ( int i, byte[] dst, int j ) {
        dst[ j ] = (byte) ( i & 0xFF );
        dst[ j + 1 ] = (byte) ( ( i >> 8 ) & 0xFF );
        dst[ j + 2 ] = (byte) ( ( i >> 16 ) & 0xFF );
        dst[ j + 3 ] = (byte) ( ( i >> 24 ) & 0xFF );
    }


    private static int dec_uint32le ( byte[] src, int si ) {
        return ( src[ si ] & 0xFF ) | ( ( src[ si + 1 ] & 0xFF ) << 8 ) | ( ( src[ si + 2 ] & 0xFF ) << 16 ) | ( ( src[ si + 3 ] & 0xFF ) << 24 );
    }


    /**
     * @param val
     * @return an SID from it's string representation
     */
    public static SID fromString ( String val ) {
        String[] parts = StringUtils.splitPreserveAllTokens(val, '-');

        if ( parts == null || parts.length < 4 ) {
            throw new IllegalArgumentException("Not a valid SID " + val); //$NON-NLS-1$
        }

        if ( !"S".equals(parts[ 0 ]) ) { //$NON-NLS-1$
            throw new IllegalArgumentException("Not a valid SID " + val); //$NON-NLS-1$
        }

        SID s = new SID();
        s.revision = Integer.parseInt(parts[ 1 ]);
        s.idAuthority = Integer.parseInt(parts[ 2 ]);

        s.subauthorities = new int[parts.length - 3];
        for ( int i = 3; i < parts.length; i++ ) {
            if ( StringUtils.isBlank(parts[ i ]) || !StringUtils.isNumeric(parts[ i ]) ) {
                throw new IllegalArgumentException("Not a valid SID " + val); //$NON-NLS-1$
            }
            s.subauthorities[ i - 3 ] = (int) Long.parseLong(parts[ i ]);
        }

        return s;
    }


    /**
     * @param val
     * @return the SID from the binary representation
     */
    public static SID fromBinary ( byte[] val ) {
        if ( val.length < 8 ) {
            throw new IllegalArgumentException("Not a valid SID, too short " + Arrays.toString(val)); //$NON-NLS-1$
        }

        SID s = new SID();
        s.revision = val[ 0 ];
        int numSubauthorities = val[ 1 ] & 0xFF;

        s.idAuthority = ( val[ 2 ] & 0xFF ) << 40L | ( val[ 3 ] & 0xFF ) << 32L | ( val[ 4 ] & 0xFF ) << 24L | ( val[ 5 ] & 0xFF ) << 16L
                | ( val[ 6 ] & 0xFF ) << 8L | ( val[ 7 ] & 0xFF );

        if ( val.length != 8 + numSubauthorities * 4 ) {
            throw new IllegalArgumentException("Not a valid SID, missing subauthorities " + Arrays.toString(val)); //$NON-NLS-1$
        }

        s.subauthorities = new int[numSubauthorities];
        for ( int i = 0; i < numSubauthorities; i++ ) {
            s.subauthorities[ i ] = dec_uint32le(val, 8 + 4 * i);
        }

        return s;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = (int) ( prime * result + this.idAuthority );
        result = prime * result + this.revision;
        result = prime * result + Arrays.hashCode(this.subauthorities);
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SID other = (SID) obj;
        if ( this.idAuthority != other.idAuthority )
            return false;
        if ( this.revision != other.revision )
            return false;
        if ( !Arrays.equals(this.subauthorities, other.subauthorities) )
            return false;
        return true;
    }

    // -GENERATED

}
