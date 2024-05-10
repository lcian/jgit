import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.zip.Deflater;


@Command(name = "hash-object",
        description = "Hash an object, possibly adding it to the object database"
)
public class HashObject implements Callable<Integer> {

    @CommandLine.Option(names = "-w",
            description = "Add the object to the database"
    )
    boolean doWrite;

    @Parameters(
            arity = "1",
            description = "Filename of the object",
            paramLabel = "<file>"
    )
    private String filename;

    @Override
    public Integer call() throws IOException {
        final byte[] contents = Files.readAllBytes(Path.of(filename));
        final byte[] object = ArrayUtils.addAll(String.format("blob %d\0", new String(contents).length()).getBytes(), contents);
        final String hash = DigestUtils.sha1Hex(object);
        System.out.println(hash);

        if (doWrite) {
            byte[] compressedObject = new byte[Constants.MAX_FILE_SIZE_BYTES];
            final Deflater compresser = new Deflater();
            compresser.setInput(object);
            compresser.finish();
            final int compressedObjectLength = compresser.deflate(compressedObject);
            compressedObject = Arrays.copyOfRange(compressedObject, 0, compressedObjectLength);

            final String hashPrefix = hash.substring(0, 2);
            final String hashSuffix = hash.substring(2);
            if (Files.notExists(Path.of(String.format(".git/objects/%s", hashPrefix)))) {
                new File(".git/objects", hashPrefix).mkdirs();
            }
            final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
            Files.createFile(Path.of(objectPath));
            Files.write(Path.of(objectPath), compressedObject);
        }
        return 0;
    }
}