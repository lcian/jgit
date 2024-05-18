package jgit.command;

import jgit.Common;
import jgit.GitCommit;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.zip.DataFormatException;

@CommandLine.Command(name = "log",
        description = "Show the commit log"
)
public class Log implements Callable<Integer> {

    @Override
    public Integer call() throws IOException, DataFormatException {
        final String masterRef = ".git/refs/heads/master";
        if (!Files.exists(Path.of(masterRef))) {
            System.out.println("No commits");
            return 0;
        }

        String commitHash = Files.readString(Path.of(masterRef)).trim();
        while (commitHash != null) {
            String hashPrefix = commitHash.substring(0, 2);
            String hashSuffix = commitHash.substring(2);
            String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
            byte[] object = Common.inflate(new FileInputStream(objectPath));
            GitCommit commit = GitCommit.deserializeUncompressed(object);
            System.out.println(commit.toString(true));
            commitHash = commit.getParentHash();
        }
        return 0;
    }
}
