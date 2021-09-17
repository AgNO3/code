/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * @author mbechler
 *
 */
public class UsernamePasswordCredential implements UnwrappedCredentials {

    private String username;
    private String password;


    /**
     * 
     */
    private UsernamePasswordCredential () {}


    /**
     * 
     * @param username
     * @param password
     */
    public UsernamePasswordCredential ( String username, String password ) {
        this.username = username;
        this.password = password;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.credentials.UnwrappedCredentials#getType()
     */
    @Override
    public CredentialType getType () {
        return CredentialType.USERNAME_PASSWORD;
    }


    /**
     * @return the username
     */
    public String getUsername () {
        return this.username;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.security.credentials.UnwrappedCredentials#encode()
     */
    @Override
    public byte[] encode () throws IOException {
        byte[] usernameBytes = null;
        byte[] passwordBytes = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.write((byte) CredentialType.USERNAME_PASSWORD.ordinal());
            dos.write((byte) 0); // version

            if ( this.username != null ) {
                dos.writeBoolean(true);
                usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
                dos.writeShort(usernameBytes.length);
                dos.write(usernameBytes);
            }
            else {
                dos.writeBoolean(false);
            }

            if ( this.password != null ) {
                dos.writeBoolean(true);
                passwordBytes = this.password.getBytes(StandardCharsets.UTF_8);
                dos.writeShort(passwordBytes.length);
                dos.write(passwordBytes);
            }
            else {
                dos.writeBoolean(false);
            }
            return bos.toByteArray();
        }
        finally {
            if ( usernameBytes != null ) {
                Arrays.fill(usernameBytes, (byte) 0);
            }
            if ( passwordBytes != null ) {
                Arrays.fill(passwordBytes, (byte) 0);
            }
        }
    }


    /**
     * @param unwrapped
     * @return unwrapped credentials
     * @throws IOException
     */
    public static UnwrappedCredentials decode ( byte[] unwrapped ) throws IOException {
        UsernamePasswordCredential upc = new UsernamePasswordCredential();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(unwrapped));

        if ( dis.read() != (byte) CredentialType.USERNAME_PASSWORD.ordinal() ) {
            throw new IOException("Invalid credential type"); //$NON-NLS-1$
        }

        int ver = dis.read();
        switch ( ver ) {
        case 0:
            return decodeVersion0(upc, dis);
        default:
            throw new IOException("Unsupported credential version"); //$NON-NLS-1$
        }

    }


    /**
     * @param upc
     * @param dis
     * @return
     * @throws IOException
     */
    private static UnwrappedCredentials decodeVersion0 ( UsernamePasswordCredential upc, DataInputStream dis ) throws IOException {
        byte[] usernameBytes = null;
        byte[] passwordBytes = null;
        try {

            boolean haveUser = dis.readBoolean();
            if ( haveUser ) {
                int userLen = ( dis.readShort() & 0xFFFF );
                usernameBytes = new byte[userLen];
                dis.readFully(usernameBytes);
                upc.username = new String(usernameBytes, StandardCharsets.UTF_8);
            }

            boolean havePass = dis.readBoolean();
            if ( havePass ) {
                int passLen = ( dis.readShort() & 0xFFFF );
                passwordBytes = new byte[passLen];
                dis.readFully(passwordBytes);
                upc.password = new String(passwordBytes, StandardCharsets.UTF_8);
            }
            return upc;
        }
        finally {
            if ( usernameBytes != null ) {
                Arrays.fill(usernameBytes, (byte) 0);
            }
            if ( passwordBytes != null ) {
                Arrays.fill(passwordBytes, (byte) 0);
            }
        }
    }

}
