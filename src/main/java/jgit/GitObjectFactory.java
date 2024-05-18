package jgit;

import org.apache.commons.lang3.NotImplementedException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class GitObjectFactory {

    public GitObject createGitObject(byte[] compressedObject) {
        final byte[] uncompressedObject = Common.inflate(new ByteArrayInputStream(compressedObject));
        int i = 0;
        while (uncompressedObject[i] != (byte) ' ') {
            i++;
        }
        final String objectType = new String(Arrays.copyOfRange(uncompressedObject, 0, i));
        return switch (objectType) {
            case "blob" -> GitBlob.deserializeUncompressed(uncompressedObject);
            case "tree" -> GitTree.deserializeUncompressed(uncompressedObject);
            case "commit" -> GitCommit.deserializeUncompressed(uncompressedObject);
            case "tag" ->
                    throw new NotImplementedException(String.format("Object type %s is not implemented yet", objectType));
            default -> throw new RuntimeException(String.format("Unexpected object type: %s", objectType));
        };
    }
}
