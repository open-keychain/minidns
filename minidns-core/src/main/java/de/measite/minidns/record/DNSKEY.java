/*
 * Copyright 2015 the original author or authors
 *
 * This software is licensed under the Apache License, Version 2.0,
 * the GNU Lesser General Public License version 2 or later ("LGPL")
 * and the WTFPL.
 * You may choose either license to govern your use of this software only
 * upon the condition that you accept all of the terms of either
 * the Apache License 2.0, the LGPL 2.1+ or the WTFPL.
 */
package de.measite.minidns.record;

import de.measite.minidns.Record.TYPE;
import de.measite.minidns.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * DNSKEY record payload.
 */
public class DNSKEY implements Data {
    /**
     * Whether the key should be used as a secure entry point key.
     * 
     * see RFC 3757
     */
    public static final short FLAG_SECURE_ENTRY_POINT = 0x1;

    /**
     * Whether the record holds a revoked key.
     */
    public static final short FLAG_REVOKE = 0x80;

    /**
     * Whether the record holds a DNS zone key.
     */
    public static final short FLAG_ZONE = 0x100;

    /**
     * Bitmap of flags: {@link #FLAG_SECURE_ENTRY_POINT}, {@link #FLAG_REVOKE}, {@link #FLAG_ZONE}.
     */
    public final short flags;

    /**
     * Must be 3 as of RFC 4034.
     */
    public final byte protocol;

    /**
     * The public key's cryptographic algorithm used.
     */
    public final byte algorithm;

    /**
     * The public key material. The format depends on the algorithm of the key being stored.
     */
    public final byte[] key;

    public DNSKEY(DataInputStream dis, byte[] data, int length) throws IOException {
        flags = dis.readShort();
        protocol = dis.readByte();
        algorithm = dis.readByte();
        key = new byte[length - 4];
        dis.readFully(key);
    }

    @Override
    public TYPE getType() {
        return TYPE.DNSKEY;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(flags);
            dos.writeByte(protocol);
            dos.writeByte(algorithm);
            dos.write(key);
        } catch (IOException e) {
            // Should never happen
            throw new IllegalStateException(e);
        }

        return baos.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(flags).append(' ')
                .append(protocol).append(' ')
                .append(algorithm).append(' ')
                .append(Base64.encodeToString(key));
        return sb.toString();
    }
}
