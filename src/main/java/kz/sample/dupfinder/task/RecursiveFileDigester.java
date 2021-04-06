package kz.sample.dupfinder.task;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import kz.sample.dupfinder.dto.FileInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

@Slf4j
public class RecursiveFileDigester extends RecursiveTask<List<FileInfo>> {

    private final List<FileInfo> fileSizeToName;
    private MessageDigest messageDigest;

    public RecursiveFileDigester(@NonNull List<FileInfo> fileSizeToName) {
        try {
            String hashAlgorithm = System.getProperty("hashAlg", "SHA-1");
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Message Digest is not initialized");
        }
        this.fileSizeToName = fileSizeToName;
    }

    @Override
    protected List<FileInfo> compute() {
        int size = fileSizeToName.size();
        int half = size / 2;

        if (size <= 5) {
            return fileSizeToName.stream()
                    .map(fileInfo -> {
                                try {
                                    fileInfo.setHash(digest(fileInfo.getName()));
                                } catch (IOException e) {
                                    System.err.println(String.format("Error on digesting file %s. Reason: %s",
                                            fileInfo.getName(), e.getMessage()));
                                }
                                return fileInfo;
                            }
                    )
                    .filter(fileInfo -> Objects.nonNull(fileInfo.getHash()))
                    .collect(Collectors.toList());
        } else {
            RecursiveFileDigester left = new RecursiveFileDigester(fileSizeToName.subList(0, half));
            RecursiveFileDigester right = new RecursiveFileDigester(fileSizeToName.subList(half, size));

            left.fork();
            List<FileInfo> computedRight = right.compute();
            computedRight.addAll(left.join());
            return computedRight;
        }
    }

    private String digest(@NonNull String fileName) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(fileName);
             DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(digestInputStream)) {
            messageDigest.reset();
            while (bufferedInputStream.read() != -1) ;
            return ByteArrayUtil.toHexString(messageDigest.digest());
        }
    }
}
