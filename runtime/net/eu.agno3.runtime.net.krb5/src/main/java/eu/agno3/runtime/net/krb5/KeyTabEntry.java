/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;


/**
 * @author mbechler
 *
 */
public class KeyTabEntry {

    private static final Logger log = Logger.getLogger(KeyTabEntry.class);

    private String realm;
    private List<String> components;
    private long nameType;
    private long timestamp;
    private long kvno;
    private byte[] keyblock;
    private int keyblockType;


    /**
     * 
     */
    public KeyTabEntry () {
        this.keyblock = new byte[0];
        this.components = new LinkedList<>();
    }


    /**
     * @param key
     */
    public KeyTabEntry ( KerberosKey key ) {
        fromKey(key);
    }


    /**
     * @param key
     */
    private final void fromKey ( KerberosKey key ) {
        KerberosPrincipal principal = key.getPrincipal();
        this.nameType = principal.getNameType();
        this.realm = principal.getRealm();

        String princName = principal.getName();
        int sepPos = princName.lastIndexOf('@');

        if ( sepPos >= 0 ) {
            princName = princName.substring(0, sepPos);
        }

        String[] princComps = StringUtils.splitPreserveAllTokens(princName, '/');
        this.components = Arrays.asList(princComps);

        this.kvno = key.getVersionNumber();
        this.timestamp = (int) ( System.currentTimeMillis() / 1000 );
        this.keyblockType = key.getKeyType();
        this.keyblock = key.getEncoded();
    }


    /**
     * @param princ
     * @param algo
     * @param password
     * @param kvno
     * @throws KerberosException
     */
    public KeyTabEntry ( KerberosPrincipal princ, String algo, String password, Integer kvno ) throws KerberosException {
        try {
            PrincipalName princName = new PrincipalName(princ.getName());
            EncryptionKey key = new EncryptionKey(password.toCharArray(), princName.getSalt(), algo);
            fromKey(new KerberosKey(princ, key.getBytes(), key.getEType(), kvno));
        }
        catch (
            RealmException |
            KrbCryptoException e ) {
            throw new KerberosException("Failed to create principal key", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the realm
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * @return the components
     */
    public List<String> getComponents () {
        return this.components;
    }


    /**
     * @return the principal of this entry
     */
    public KerberosPrincipal getPrincipal () {
        return new KerberosPrincipal(getPrincipalName(), (int) this.nameType);
    }


    /**
     * @return
     */
    private String getPrincipalName () {
        return String.format("%s@%s", StringUtils.join(this.components, '/'), this.realm); //$NON-NLS-1$
    }


    /**
     * @return the timestamp
     */
    public long getTimestamp () {
        return this.timestamp;
    }


    /**
     * @return the kvno
     */
    public long getKvno () {
        return this.kvno;
    }


    /**
     * @return the keyblockType
     */
    public int getKeyblockType () {
        return this.keyblockType;
    }


    /**
     * @return the keyblock
     */
    public byte[] getKeyblock () {
        if ( this.keyblock == null ) {
            return null;
        }
        return Arrays.copyOf(this.keyblock, this.keyblock.length);
    }


    /**
     * 
     * @return the stored kerberos key
     */
    public KerberosKey getKey () {
        return new KerberosKey(this.getPrincipal(), getKeyblock(), getKeyblockType(), (int) getKvno());
    }


    /**
     * @param os
     * @throws IOException
     */
    public void write ( DataOutputStream os ) throws IOException {

        int numComponents = 0;
        int componentsSize = 0;

        for ( String component : this.components ) {
            numComponents++;
            componentsSize += 2 + component.length();
        }

        int size = 2 + ( 2 + this.realm.length() ) + componentsSize + 4 + 4 + 1 + 2 + ( 2 + this.keyblock.length ) + 4;

        os.writeInt(size);

        os.writeShort(numComponents);

        os.writeShort(this.realm.length());
        os.writeBytes(this.realm);

        for ( String component : this.components ) {
            os.writeShort(component.length());
            os.writeBytes(component);
        }

        os.writeInt((int) this.nameType);
        os.writeInt((int) this.timestamp);
        os.writeByte((byte) this.kvno);

        os.writeShort(this.keyblockType);
        os.writeShort(this.keyblock.length);
        os.write(this.keyblock);

        os.writeInt((int) this.kvno);
    }


    /**
     * @param is
     * @return the parsed keytab entry
     * @throws IOException
     */
    public static KeyTabEntry parse ( DataInputStream is ) throws IOException {
        KeyTabEntry e = new KeyTabEntry();

        int size = -1;
        try {
            while ( size < 0 ) {
                size = is.readInt();
                if ( size < 0 ) {
                    is.skip(size * -1);
                }
            }
        }
        catch ( EOFException ex ) {
            log.trace("EOF found", ex); //$NON-NLS-1$
            return null;
        }

        int numComponents = is.readUnsignedShort();
        e.realm = readString(is);
        e.components = new LinkedList<>();

        int read = 2 + 2 + e.realm.length();

        for ( int i = 0; i < numComponents; i++ ) {
            String component = readString(is);
            read += component.length() + 2;
            e.components.add(component);
        }

        e.nameType = is.readInt();
        e.timestamp = is.readInt();
        e.kvno = is.readUnsignedByte();

        read += 4 + 4 + 1;

        e.keyblockType = is.readUnsignedShort();
        e.keyblock = readOctetString(is);

        read += e.keyblock.length + 2 + 2;

        if ( size - read >= 4 ) {
            e.kvno = is.readInt();
            read += 4;
        }

        int remain = size - read;
        if ( remain > 0 ) {
            is.skip(remain);
        }
        else if ( remain < 0 ) {
            throw new IOException("Failed to read keytab entry"); //$NON-NLS-1$
        }

        return e;
    }


    /**
     * @param is
     * @return
     * @throws IOException
     */
    private static String readString ( DataInputStream is ) throws IOException {
        return new String(readOctetString(is), Charset.forName("US-ASCII")); //$NON-NLS-1$
    }


    /**
     * @param is
     * @return
     * @throws IOException
     */
    private static byte[] readOctetString ( DataInputStream is ) throws IOException {
        int length = is.readUnsignedShort();
        byte[] data = new byte[length];
        is.readFully(data);
        return data;
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
        result = prime * result + ( ( this.components == null ) ? 0 : this.components.hashCode() );
        result = prime * result + Arrays.hashCode(this.keyblock);
        result = prime * result + this.keyblockType;
        result = prime * result + (int) ( this.kvno ^ ( this.kvno >>> 32 ) );
        result = prime * result + (int) ( this.nameType ^ ( this.nameType >>> 32 ) );
        result = prime * result + ( ( this.realm == null ) ? 0 : this.realm.hashCode() );
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
        KeyTabEntry other = (KeyTabEntry) obj;
        if ( this.components == null ) {
            if ( other.components != null )
                return false;
        }
        else if ( !this.components.equals(other.components) )
            return false;
        if ( !Arrays.equals(this.keyblock, other.keyblock) )
            return false;
        if ( this.keyblockType != other.keyblockType )
            return false;
        if ( this.kvno != other.kvno )
            return false;
        if ( this.nameType != other.nameType )
            return false;
        if ( this.realm == null ) {
            if ( other.realm != null )
                return false;
        }
        else if ( !this.realm.equals(other.realm) )
            return false;
        return true;
    }

    // -GENERATED
}
