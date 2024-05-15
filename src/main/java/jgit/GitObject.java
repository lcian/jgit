package jgit;

import java.io.InputStream;
import java.io.OutputStream;

public interface GitObject {

    public static GitObject deserializeUncompressed(byte[] in) {
        throw new UnsupportedOperationException();
    }

    public static GitObject deserialize(InputStream in) {
        return deserializeUncompressed(Common.inflate(in));
    }

    public String getType();

    public int getSize();

    public byte[] getHash();

    public String getHashHex();

    public byte[] getUncompressedSerialization();

    public default void serialize(OutputStream out) {
        Common.deflate(this.getUncompressedSerialization(), out);
    }
};
