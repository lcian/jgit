//import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.lang3.ArrayUtils;
//import picocli.CommandLine;
//import picocli.CommandLine.Command;
//import picocli.CommandLine.Parameters;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.concurrent.Callable;
//import java.util.zip.Deflater;
//import java.util.zip.Inflater;
//
//
//@Command(name = "ls-tree",
//        description = "List the contents of a tree object"
//)
//public class LsTree implements Callable<Integer> {
//
//    @Parameters(
//            arity = "1",
//            description = "Hash of the tree",
//            paramLabel = "<tree>"
//    )
//    private String treeHash;
//
//    @Override
//    public Integer call() throws IOException {
//        final String objectHashPrefix = objectHash.substring(0, 2);
//        final String objectHashSuffix = objectHash.substring(2);
//        final String objectPath = String.format(".git/objects/%s/%s", objectHashPrefix, objectHashSuffix);
//        final byte[] compressedObject = Files.readAllBytes(Path.of(objectPath));
//        byte[] objectBytes = new byte[jgit.Constants.MAX_FILE_SIZE_BYTES];
//        final Inflater decompresser = new Inflater();
//        decompresser.setInput(compressedObject);
//        decompresser.inflate(objectBytes);
//        final String object = new String(objectBytes);
//        final String hash = DigestUtils.sha1Hex(object);
//        System.out.println(hash);
//
//        if (doWrite) {
//            byte[] compressedObject = new byte[jgit.Constants.MAX_FILE_SIZE_BYTES];
//            final Deflater compresser = new Deflater();
//            compresser.setInput(object);
//            compresser.finish();
//            final int compressedObjectLength = compresser.deflate(compressedObject);
//            compressedObject = Arrays.copyOfRange(compressedObject, 0, compressedObjectLength);
//
//            final String hashPrefix = hash.substring(0, 2);
//            final String hashSuffix = hash.substring(2);
//            if (Files.notExists(Path.of(String.format(".git/objects/%s", hashPrefix)))) {
//                new File(".git/objects", hashPrefix).mkdirs();
//            }
//            final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
//            Files.createFile(Path.of(objectPath));
//            Files.write(Path.of(objectPath), compressedObject);
//        }
//        return 0;
//    }
//}