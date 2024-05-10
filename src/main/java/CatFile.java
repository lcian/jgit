import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


@Command(name = "cat-file",
        description = "Show information about an object"
)
public class CatFile implements Callable<Integer> {

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
        decompresser.inflate(objectBytes);
        final String object = new String(objectBytes);
        final String objectType = object.split(" ", 2)[0];
        final String objectSize = object.split(" ", 2)[1].split("\0", 2)[0];
        final String objectContents = object.split(" ", 2)[1].split("\0", 2)[1].trim();

        if (showFlag.t) {
            System.out.println(objectType);
        } else if (showFlag.s) {
            System.out.println(objectSize);
        } else if (showFlag.p) {
            System.out.println(objectContents);
        }
        return 0;
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
