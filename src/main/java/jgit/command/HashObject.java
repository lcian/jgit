package jgit.command;

import jgit.GitBlob;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;


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
        final GitBlob blob = new GitBlob(contents);
        final String hash = blob.getHashHex();
        System.out.println(hash);

        if (doWrite) {
            final String hashPrefix = hash.substring(0, 2);
            final String hashSuffix = hash.substring(2);
            if (Files.notExists(Path.of(String.format(".git/objects/%s", hashPrefix)))) {
                new File(".git/objects", hashPrefix).mkdirs();
            }
            final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
            final FileOutputStream out = new FileOutputStream(objectPath);
            blob.serialize(out);
            out.close();
        }
        return 0;
    }
}