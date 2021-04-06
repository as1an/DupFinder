package kz.sample.dupfinder;

import kz.sample.dupfinder.dto.FileInfo;
import kz.sample.dupfinder.task.RecursiveFileDigester;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
class DupFinder {

    private final String initialDir;

    public DupFinder(String initialDir) {
        this.initialDir = initialDir;
    }

    public void searchForDuplicates() {

        Map<Long, List<FileInfo>> sizeToFiles = obtainFilesInDirectory();

        List<FileInfo> fileInfos = sizeToFiles.values().stream().filter(infos -> infos.size() > 1)
                .map(ArrayList::new)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        ForkJoinPool pool = ForkJoinPool.commonPool();
        List<FileInfo> digestedFileInfos = pool.invoke(new RecursiveFileDigester(fileInfos));
        pool.shutdown();

        sizeToFiles.clear();

        Map<String, List<String>> hashToFiles = digestedFileInfos.stream()
                .collect(Collectors.groupingBy(FileInfo::getHash,
                        Collectors.mapping(FileInfo::getName, Collectors.toList())))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        hashToFiles.forEach((key, value) -> log.info(
                String.join(" ", value)));

    }

    private Map<Long, List<FileInfo>> obtainFilesInDirectory() {
        Map<Long, List<FileInfo>> sizeToFiles = new ConcurrentHashMap<>();
        try {
            Files.walkFileTree(Paths.get(initialDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!Files.isDirectory(file) && attrs.size() > 0) {
                        sizeToFiles.compute(attrs.size(), (size, filename) -> {
                            List<FileInfo> files = Optional.ofNullable(sizeToFiles.get(attrs.size())).
                                    orElseGet(LinkedList::new);
                            FileInfo fileInfo = FileInfo.builder()
                                    .size(size)
                                    .name(file.toAbsolutePath().toString())
                                    .build();
                            files.add(fileInfo);
                            return files;
                        });
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.printf("Error on walking file tree. Reason: %s%n", e.getMessage());
        }
        return sizeToFiles;
    }

}
