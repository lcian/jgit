package jgit;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import jgit.command.HashObject;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;


public class GitIndex {

    private static final int VERSION = 2;
    private static final String SIGNATURE = "DIRC";
    private SortedSet<Entry> entries;

    public GitIndex() {
        this.entries = new TreeSet<>(Entry.relativePathComparator);
    }

    private GitIndex(Collection<Entry> entries) {
        this();
        this.entries.addAll(entries);
    }

    public static GitIndex deserialize(InputStream in) {
        final byte[] bytes;
        try {
            bytes = in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String signature = new String(Arrays.copyOfRange(bytes, 0, 4));
        if (!signature.equals(SIGNATURE)) {
            throw new RuntimeException(String.format("Unexpected signature: %s", signature));
        }
        final int version = Ints.fromByteArray(Arrays.copyOfRange(bytes, 4, 8));
        if (version != VERSION) {
            throw new UnsupportedOperationException(String.format("Version %d is not supported", version));
        }
        final int numEntries = Ints.fromByteArray(Arrays.copyOfRange(bytes, 8, 12));
        final SortedSet<Entry> entries = new TreeSet<Entry>(Entry.relativePathComparator);
        int i = 12; // skip signature, version, number of entries
        while ((i < bytes.length) && entries.size() < numEntries) {
            int j = i + 62; // 62 = size of an entry except for variable relpath
            while (!((bytes[j] == (byte) '\0') && (j % 8 == 0))) { // reach end of padded relpath
                j++;
            }
            int new_i = j + 1; // where the actual new entry starts
            while (bytes[j] == (byte) '\0') { // ignore padding zeros
                j--;
            }
            entries.add(Entry.deserialize(new ByteArrayInputStream(ArrayUtils.add(Arrays.copyOfRange(bytes, i, j + 1), (byte) '\0'))));
            i = new_i;
        }
        return new GitIndex(entries);
    }

    public Iterator<String> getObjectRelativePaths() {
        Vector<String> relpaths = new Vector<>();
        for (Entry entry : entries) {
            relpaths.addLast(entry.relativePath);
        }
        return relpaths.iterator();
    }

    private void add(Entry entry) {
        entries.add(entry);
    }

    public void add(String file) {
        entries.add(Entry.of(file));
    }

    public void remove(String file) {
        throw new UnsupportedOperationException("Unsupported");
    }

    public void serialize(OutputStream out) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(SIGNATURE.getBytes());
            baos.write(Ints.toByteArray(VERSION));
            baos.write(Ints.toByteArray(this.entries.size()));
            for (Entry entry : entries) {
                entry.serialize(baos);
            }
            baos.writeTo(out);
            out.write(DigestUtils.sha1(baos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("GitIndex{%n"));
        for (final Entry entry : this.entries) {
            sb.append(String.format("%s%n", entry.toString()));
        }
        sb.append('}');
        return sb.toString();
    }

    private static class Entry {
        private static final Comparator<Entry> relativePathComparator = Comparator.comparing(Entry::getRelativePath);

        //ctime (8 bytes): The last time the file's metadata changed.
        FileTime creationTime;
        //mtime (8 bytes): The last time the file's content changed.
        FileTime modifiedTime;
        //dev (4 bytes): The device ID.
        int deviceId;
        //ino (4 bytes): The inode number.
        int inode;
        //mode (4 bytes): The file mode (regular file, symlink, executable, etc.).
        int mode;
        //uid (4 bytes): The user ID of the file owner.
        int uid;
        //gid (4 bytes): The group ID of the file owner.
        int gid;
        //size (4 bytes): The size of the file (in bytes).
        int size;
        //SHA-1 (20 bytes): The SHA-1 hash of the file's content.
        byte[] sha1;
        //Flags (2 bytes): Flags for indicating the stage and other attributes.
        short flags;
        //Path (variable length): The relative path of the file (null-terminated). The path is relative to the root of the repository.
        String relativePath;

        public Entry(FileTime creationTime, FileTime modifiedTime, int deviceId, int inode, int mode, int uid, int gid, int size, byte[] sha1, short flags, String relativePath) {
            this.creationTime = creationTime;
            this.modifiedTime = modifiedTime;
            this.deviceId = deviceId;
            this.inode = inode;
            this.mode = mode;
            this.uid = uid;
            this.gid = gid;
            this.size = size;
            this.sha1 = sha1;
            this.flags = flags;
            this.relativePath = relativePath;
        }

        public static Entry deserialize(InputStream in) {
            FileTime creationTime, modifiedTime;
            int deviceId, inode, mode, uid, gid, size;
            byte[] sha1;
            short flags;
            String relativePath;
            try {
                creationTime =
                        FileTime.fromMillis(Longs.fromByteArray(in.readNBytes(8)));
                modifiedTime =
                        FileTime.fromMillis(Longs.fromByteArray(in.readNBytes(8)));
                deviceId = Ints.fromByteArray(in.readNBytes(4));
                inode = Ints.fromByteArray(in.readNBytes(4));
                mode = Ints.fromByteArray(in.readNBytes(4));
                uid = Ints.fromByteArray(in.readNBytes(4));
                gid = Ints.fromByteArray(in.readNBytes(4));
                size = Ints.fromByteArray(in.readNBytes(4));
                sha1 = in.readNBytes(20);
                flags = Shorts.fromByteArray(in.readNBytes(2));
                byte[] remainingBytes = in.readAllBytes();
                relativePath = new String(Arrays.copyOfRange(remainingBytes, 0, remainingBytes.length - 1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new Entry(creationTime, modifiedTime, deviceId, inode, mode, uid, gid, size, sha1, flags, relativePath);
        }

        public static Entry of(String file) {
            final Path path = Path.of(file);
            final String absolutePathString = path.toAbsolutePath().toString();
            byte[] contents;
            try {
                contents = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to access file %s", file));
            }
            BasicFileAttributes attributes;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to access attributes of file %s", file));
            }
            return new Entry(
                    attributes.creationTime(),
                    attributes.lastModifiedTime(),
                    (int) FileUtils.getDeviceId(absolutePathString),
                    (int) FileUtils.getInode(absolutePathString),
                    FileUtils.getMode(absolutePathString),
                    FileUtils.getUid(absolutePathString),
                    FileUtils.getGid(absolutePathString),
                    (int) attributes.size(),
                    DigestUtils.sha1(contents),
                    (short) 5, // flags
                    file
            );
        }

        public String getRelativePath() {
            return this.relativePath;
        }

        public void serialize(OutputStream out) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                byte[] creationTime = ArrayUtils.addAll(
                        Arrays.copyOfRange(Longs.toByteArray(this.creationTime.toInstant().getEpochSecond()), 4, 8),
                        Arrays.copyOfRange(Longs.toByteArray(this.creationTime.toInstant().getNano()), 4, 8)
                );
                baos.write(creationTime);
                byte[] modifiedTime = ArrayUtils.addAll(
                        Arrays.copyOfRange(Longs.toByteArray(this.modifiedTime.toInstant().getEpochSecond()), 4, 8),
                        Arrays.copyOfRange(Longs.toByteArray(this.modifiedTime.toInstant().getNano()), 4, 8)
                );
                baos.write(modifiedTime);
                baos.write(Ints.toByteArray(this.deviceId));
                baos.write(Ints.toByteArray(this.inode));
                baos.write(Ints.toByteArray(this.mode));
                baos.write(Ints.toByteArray(this.uid));
                baos.write(Ints.toByteArray(this.gid));
                baos.write(Ints.toByteArray(this.size));

                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                HashObject hashObject = new HashObject(this.relativePath, true);
                hashObject.call(baos2);
                baos.write(Hex.decodeHex(baos2.toString()));

                // baos.write(Shorts.toByteArray(this.flags));
                baos.write(Shorts.toByteArray((short) 5)); // flags
                baos.write(this.relativePath.getBytes());
                int written = baos.size();
                do {
                    baos.write((byte) '\0');
                    written++;
                } while (written % 8 != 0);
                baos.writeTo(out);
            } catch (IOException | DecoderException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Entry{");
            sb.append(String.format("creationTime=%s, ", creationTime.toString()));
            sb.append(String.format("modifiedTime=%s, ", modifiedTime.toString()));
            sb.append(String.format("deviceId=%d, ", deviceId));
            sb.append(String.format("inode=%d, ", inode));
            sb.append(String.format("mode=%d, ", mode));
            sb.append(String.format("uid=%d, ", uid));
            sb.append(String.format("gid=%d, ", gid));
            sb.append(String.format("size=%d, ", size));
            sb.append(String.format("sha1=%s, ", Hex.encodeHexString(sha1)));
            sb.append(String.format("flags=%d, ", flags));
            sb.append(String.format("relativePath='%s'", relativePath));
            sb.append("}");
            return sb.toString();
        }
    }
}
