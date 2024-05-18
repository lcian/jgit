package jgit.command;

import jgit.GitObject;
import jgit.GitObjectFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.zip.DataFormatException;


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
    private String hash;

    @Override
    public Integer call() throws IOException, DataFormatException {
        final String hashPrefix = hash.substring(0, 2);
        final String hashSuffix = hash.substring(2);
        final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
        if (showFlag.e) {
            return Files.exists(Path.of(objectPath)) ? 0 : 1;
        }

        final byte[] compressedObject = Files.readAllBytes(Path.of(objectPath));
        final GitObjectFactory factory = new GitObjectFactory();
        final GitObject object = factory.createGitObject(compressedObject);

        if (showFlag.t) {
            System.out.println(object.getType());
        } else if (showFlag.s) {
            System.out.println(object.getSize());
        } else if (showFlag.p) {
            System.out.print(object);
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
