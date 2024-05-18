package jgit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

public class GitCommit {

    private String parentHash;
    private String treeHash;
    private String authorName;
    private String authorEmail;
    private Instant creationTimestamp;
    private String committerName;
    private String committerEmail;
    private Instant commitmentTimestamp;
    private String message;

    public GitCommit(String parentHash, String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String committerName, String committerEmail, Instant commitmentTimestamp, String message) {
        this.parentHash = parentHash;
        this.treeHash = treeHash;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.creationTimestamp = creationTimestamp;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.commitmentTimestamp = commitmentTimestamp;
        this.message = message;
    }

    public GitCommit(String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String committerName, String committerEmail, Instant commitmentTimestamp, String message) {
        this(null, treeHash, authorName, authorEmail, creationTimestamp, committerName, committerEmail, commitmentTimestamp, message);
    }

    public GitCommit(String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String message) {
        this(null, treeHash, authorName, authorEmail, creationTimestamp, authorName, authorEmail, creationTimestamp, message);
    }

    public GitCommit(String parentHash, String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String message) {
        this(parentHash, treeHash, authorName, authorEmail, creationTimestamp, authorName, authorEmail, creationTimestamp, message);
    }

    public String getHashHex() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.serialize(baos);
        byte[] serialized = baos.toByteArray();
        return DigestUtils.sha1Hex(serialized);
    }

    public void serialize(OutputStream out) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(String.format("tree %s%n", this.treeHash).getBytes());
            if (this.parentHash != null) {
                baos.write(String.format("parent %s%n", this.parentHash).getBytes());
            }
            baos.write(String.format("author %s <%s> %s%n", this.authorName, this.authorEmail, this.creationTimestamp.getEpochSecond()).getBytes());
            baos.write(String.format("committer %s <%s> %s +0200%n", this.committerName, this.committerEmail, this.commitmentTimestamp.getEpochSecond()).getBytes());
            baos.write(String.format("%n%s%n", this.message).getBytes());
            byte[] body = baos.toByteArray();
            out.write(String.format("commit %d\0", body.length).getBytes());
            out.write(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
