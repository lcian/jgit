package jgit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class GitBlob implements GitObject {

    private final byte[] contents;

    public GitBlob(byte[] contents) {
        this.contents = contents.clone();
    }

    public static GitBlob deserializeUncompressed(byte[] in) {
        if (in.length < 4 || !new String(Arrays.copyOfRange(in, 0, 4)).equals("blob")) {
            throw new RuntimeException("Not a blob object");
        }
        int i = 0;
        while (in[i] != (byte) '\0') {
            i++;
        }
        i++;
        final byte[] contents = Arrays.copyOfRange(in, i, in.length);
        return new GitBlob(contents);
    }

    public String getType() {
        return "blob";
    }

    public int getSize() {
        return this.contents.length;
    }

    public byte[] getHash() {
        return DigestUtils.sha1(this.getUncompressedSerialization());
    }

    public String getHashHex() {
        return DigestUtils.sha1Hex(this.getUncompressedSerialization());
    }

    public String toString() {
        return new String(this.contents);
    }

    public byte[] getUncompressedSerialization() {
        return ArrayUtils.addAll(
                String.format("blob %d\0", this.contents.length).getBytes(),
                this.contents
        );
    }
}
