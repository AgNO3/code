/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.scrypt;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;


/**
 * Wrapper for key/salt
 * 
 * @author mbechler
 *
 */
public class SCryptResult {

    private final String salt;
    private final byte[] key;


    /**
     * @param salt
     * @param key
     */
    public SCryptResult ( String salt, byte[] key ) {
        this.salt = salt;
        this.key = Arrays.copyOf(key, key.length);
    }


    /**
     * @return the salt
     */
    public String getSalt () {
        return this.salt;
    }


    /**
     * @return the key
     */
    public byte[] getKey () {
        return Arrays.copyOf(this.key, this.key.length);
    }


    /**
     * @return string representation of salt and key
     * @throws IOException
     */
    public String export () throws IOException {
        return SCryptUtil.B64ENC.encodeToString(exportBinary());
    }


    /**
     * @return
     * @throws IOException
     */
    private byte[] exportBinary () throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(this.salt);
        dos.writeInt(this.key.length);
        dos.write(this.key);
        return bos.toByteArray();
    }


    /**
     * 
     * @param data
     * @return decoded SCryptResult
     * @throws IOException
     */
    public static SCryptResult importFrom ( String data ) throws IOException {
        return importFrom(SCryptUtil.B64DEC.decode(data));
    }


    /**
     * @param bd
     * @return decoded SCryptResult
     * @throws IOException
     */
    public static SCryptResult importFrom ( byte[] bd ) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bd);
        DataInputStream dis = new DataInputStream(bis);
        String salt = dis.readUTF();
        int keylen = dis.readInt();
        if ( keylen <= 0 || keylen > 512 ) {
            throw new IOException("Invalid key length"); //$NON-NLS-1$
        }
        byte[] key = new byte[keylen];
        dis.readFully(key);
        return new SCryptResult(salt, key);
    }
}