package jgit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Common {
    public static byte[] getContents(String hash) {
        final String hashPrefix = hash.substring(0, 2);
        final String hashSuffix = hash.substring(2);
        final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
        try {
            return Files.readAllBytes(Path.of(objectPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] inflate(InputStream in) {
        try {
            final byte[] deflated = in.readAllBytes();
            byte[] inflated = new byte[Constants.MAX_FILE_SIZE_BYTES];
            final Inflater inflater = new Inflater();
            inflater.setInput(deflated);
            final int inflatedLength = inflater.inflate(inflated);
            return Arrays.copyOfRange(inflated, 0, inflatedLength);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deflate(byte[] inflated, OutputStream out) {
        byte[] deflated = new byte[Constants.MAX_FILE_SIZE_BYTES];
        final Deflater deflater = new Deflater();
        deflater.setInput(inflated);
        deflater.finish();
        final int deflatedLength = deflater.deflate(deflated);
        deflater.end();
        deflated = Arrays.copyOfRange(deflated, 0, deflatedLength);
        try {
            out.write(deflated);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
