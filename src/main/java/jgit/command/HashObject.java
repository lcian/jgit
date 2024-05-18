package jgit.command;

import jgit.GitBlob;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;


@Command(name = "hash-object",
        description = "Hash an object, possibly adding it to the object database"
)
public class HashObject implements Callable<Integer> {

    @Parameters(
            arity = "1",
            description = "Filename of the object",
            paramLabel = "<file>"
    )
    private String filename;

    @CommandLine.Option(names = "-w",
            description = "Add the object to the database"
    )
    private boolean doWrite;

    public HashObject(String filename, boolean doWrite) {
        this.filename = filename;
        this.doWrite = doWrite;
    }

    public Integer call(OutputStream out) throws IOException {
        final byte[] contents = Files.readAllBytes(Path.of(this.filename));
        final GitBlob blob = new GitBlob(contents);
        final String hash = blob.getHashHex();
        out.write(hash.getBytes());

        if (this.doWrite) {
            final String hashPrefix = hash.substring(0, 2);
            final String hashSuffix = hash.substring(2);
            if (Files.notExists(Path.of(String.format(".git/objects/%s", hashPrefix)))) {
                new File(".git/objects", hashPrefix).mkdirs();
            }
            final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
            final FileOutputStream fos = new FileOutputStream(objectPath);
            blob.serialize(fos);
            fos.close();
        }
        return 0;
    }

    @Override
    public Integer call() throws IOException {
        final int res = this.call(System.out);
        System.out.println();
        return res;
    }
}