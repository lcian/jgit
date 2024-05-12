import org.apache.commons.lang3.NotImplementedException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


@Command(name = "cat-file",
        description = "Show information about an object"
)
public class CatFile implements Callable<Integer> {

    @Override
    public Integer call() throws IOException, DataFormatException {
        final String objectHashPrefix = objectHash.substring(0, 2);
        final String objectHashSuffix = objectHash.substring(2);
        final String objectPath = String.format(".git/objects/%s/%s", objectHashPrefix, objectHashSuffix);
        if (showFlag.e) {
            return Files.exists(Path.of(objectPath)) ? 0 : 1;
        }

        final byte[] compressedObject = Files.readAllBytes(Path.of(objectPath));
        byte[] objectBytes = new byte[Constants.MAX_FILE_SIZE_BYTES];
        final Inflater decompresser = new Inflater();
        decompresser.setInput(compressedObject);
        int decompressedObjectLength = decompresser.inflate(objectBytes);
        objectBytes = Arrays.copyOfRange(objectBytes, 0, decompressedObjectLength);
        /*
         * An object is stored in the following format
         * <header>\0<content>
         * where <header> is of the form:
         * blob|tree|commit <content_length>
         */
        final String object = new String(objectBytes);
        final String objectType = object.split(" ", 2)[0];
        final String objectSize = object.split(" ", 2)[1].split("\0", 2)[0];
        final String objectContents = object.split(" ", 2)[1].split("\0", 2)[1].trim();

        if (showFlag.t) {
            System.out.println(objectType);
        } else if (showFlag.s) {
            System.out.println(objectSize);
        } else if (showFlag.p) {
            switch (objectType) {
                case "blob" -> System.out.println(objectContents);
                case "tree" -> {
                    /*
                     * A tree contains a number of entries of the form:
                     * <mode> <name>\0<hash>
                     */
                    final Vector<TreeEntry> entries = new Vector<>();
                    StringBuilder sb = new StringBuilder();
                    int i = 4 + 1 + objectSize.length() + 1; // skip header
                    while (i < objectBytes.length) {
                        while ((char) objectBytes[i] != ' ') {
                            sb.append((char) objectBytes[i]);
                            i += 1;
                        }
                        i += 1;
                        int mode = Integer.parseInt(sb.toString());
                        sb.setLength(0);

                        while ((char) objectBytes[i] != '\0') {
                            sb.append((char) objectBytes[i]);
                            i += 1;
                        }
                        i += 1;
                        String name = sb.toString();
                        sb.setLength(0);

                        for (int j = i; j < i + 20; j++) {
                            sb.append(String.format("%02x", objectBytes[j]));
                        }
                        i += 20;
                        String hash = sb.toString();
                        sb.setLength(0);

                        entries.addElement(new TreeEntry(mode, name, hash));
                    }
                    for (TreeEntry entry : entries) {
                        System.out.println(entry);
                    }
                }
                case "commit" -> throw new NotImplementedException("Commits are not yet implemented");
                default -> throw new RuntimeException(String.format("Unexpected object type: %s", objectType));
            }
        }
        return 0;
    }

    @CommandLine.ArgGroup(
            heading = "What information to show%n",
            multiplicity = "1"
    )
    ShowFlag showFlag;
    @Parameters(
            arity = "1",
            description = "Hash of the object",
            paramLabel = "<object>"
    )
    private String objectHash;

    public static final class TreeEntry {
        private final int mode;
        private final String name;
        private final String hash;

        public TreeEntry(int mode, String name, String hash) {
            this.mode = mode;
            this.name = name;
            this.hash = hash;
        }

        @Override
        public String toString() {
            return String.format("%06d %s %s\t%s", this.mode, this.mode == 40000 ? "tree" : "blob", this.hash, this.name);
        }
    }

    static class ShowFlag {
        @CommandLine.Option(names = "-e", description = "Check if object exists")
        boolean e;
        @CommandLine.Option(names = "-t", description = "Show object type")
        boolean t;
        @CommandLine.Option(names = "-s", description = "Show object size")
        boolean s;
        @CommandLine.Option(names = "-p", description = "Print object contents")
        boolean p;
    }
}
