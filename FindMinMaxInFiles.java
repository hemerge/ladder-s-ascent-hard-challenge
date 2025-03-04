import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

/**
Challenge:
You're given a folder containing 15,000 text files, each filled with 15,000 random integers separated by newline ranging from -2^63 to 2^63 - 1. Your task is to efficiently determine the minimum and maximum integers across all 15,000 × 15,000 numbers.

You can use any language, any paradigm, any framework, anything; speed is the only thing that matters.
**/
public class FindMinMaxInFiles {
    private static final String FOLDER_PATH = "challenge-1";
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int BUFFER_SIZE = 65_536;
    private static final int BATCH_SIZE = 10;
    private static final int PROGRESS_BAR_LENGTH = 50;

    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();

        List<Path> filePaths = Files.list(Paths.get(FOLDER_PATH)).toList();
        int totalFiles = filePaths.size();
        AtomicInteger filesProcessed = new AtomicInteger(0);

        ForkJoinPool forkJoinPool = new ForkJoinPool(NUM_THREADS);
        ExecutorService executorService = Executors.newCachedThreadPool();

        List<CompletableFuture<long[]>> futures = new ArrayList<>();

        for (int i = 0; i < filePaths.size(); i += BATCH_SIZE) {
            List<Path> batch = filePaths.subList(i, Math.min(i + BATCH_SIZE, filePaths.size()));

            futures.add(CompletableFuture.supplyAsync(() ->
                forkJoinPool.invoke(new ProcessBatch(batch, filesProcessed, totalFiles)), executorService));
        }

        long globalMin = Long.MAX_VALUE;
        long globalMax = Long.MIN_VALUE;

        for (CompletableFuture<long[]> future : futures) {
            long[] result = future.get();
            globalMin = Math.min(globalMin, result[0]);
            globalMax = Math.max(globalMax, result[1]);
        }

        forkJoinPool.shutdown();
        executorService.shutdown();

        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1e9;

        System.out.print("\r" + " ".repeat(PROGRESS_BAR_LENGTH + 26) + "\r");
        System.out.println("Processing Complete!");
        System.out.println("Global Min: " + globalMin);
        System.out.println("Global Max: " + globalMax);
        System.out.printf("Time Taken: %.4f seconds%n", elapsedTime);

        // Print System Configuration
        printSystemInfo();
    }

    static class ProcessBatch extends RecursiveTask<long[]> {
        private final List<Path> files;
        private final AtomicInteger filesProcessed;
        private final int totalFiles;

        public ProcessBatch(List<Path> files, AtomicInteger filesProcessed, int totalFiles) {
            this.files = files;
            this.filesProcessed = filesProcessed;
            this.totalFiles = totalFiles;
        }

        @Override
        protected long[] compute() {
            long batchMin = Long.MAX_VALUE;
            long batchMax = Long.MIN_VALUE;

            for (Path file : files) {
                long[] minMax = findMinMaxInFile(file);
                batchMin = Math.min(batchMin, minMax[0]);
                batchMax = Math.max(batchMax, minMax[1]);

                updateProgress(filesProcessed.incrementAndGet(), totalFiles);
            }
            return new long[]{batchMin, batchMax};
        }
    }

    private static long[] findMinMaxInFile(Path filePath) {
        long localMin = Long.MAX_VALUE;
        long localMax = Long.MIN_VALUE;

        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            byte[] tempBuffer = new byte[BUFFER_SIZE];
            StringBuilder leftover = new StringBuilder();

            while (buffer.hasRemaining()) {
                int bytesRead = Math.min(buffer.remaining(), BUFFER_SIZE);
                buffer.get(tempBuffer, 0, bytesRead);

                String chunk = new String(tempBuffer, 0, bytesRead, StandardCharsets.UTF_8);
                String[] numbers = chunk.split("\n");

                if (leftover.length() > 0) {
                    numbers[0] = leftover.append(numbers[0]).toString();
                    leftover.setLength(0);
                }

                if (!chunk.endsWith("\n")) {
                    leftover.append(numbers[numbers.length - 1]);
                }

                int limit = chunk.endsWith("\n") ? numbers.length : numbers.length - 1;
                for (int i = 0; i < limit; i++) {
                    long num = Long.parseLong(numbers[i].trim());
                    localMin = Math.min(localMin, num);
                    localMax = Math.max(localMax, num);
                }
            }

            if (leftover.length() > 0) {
                long num = Long.parseLong(leftover.toString().trim());
                localMin = Math.min(localMin, num);
                localMax = Math.max(localMax, num);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new long[]{localMin, localMax};
    }

    private static void updateProgress(int filesProcessed, int totalFiles) {
        double progress = (double) filesProcessed / totalFiles;
        int completedBars = (int) (progress * PROGRESS_BAR_LENGTH);
        int remainingBars = PROGRESS_BAR_LENGTH - completedBars;

        String progressBar = "[" + "█".repeat(completedBars) + "-".repeat(remainingBars) + "]";
        System.out.print("\rProcessing: " + progressBar + String.format(" %d/%d", filesProcessed, totalFiles));
    }

    private static void printSystemInfo() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        long totalDiskSpace = 0;
        long freeDiskSpace = 0;
        String storageType = "Unknown";

        try {
            FileStore fileStore = Files.getFileStore(Paths.get(System.getProperty("user.dir")));
            totalDiskSpace = fileStore.getTotalSpace();
            freeDiskSpace = fileStore.getUsableSpace();
            storageType = fileStore.supportsFileAttributeView("ssd") ? "SSD" : "HDD";
        } catch (IOException e) {
            System.err.println("Error retrieving disk space information: " + e.getMessage());
        }

        System.out.println("\nSystem Configuration:");
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("CPU Cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max Threads: " + NUM_THREADS);
        System.out.println("RAM: " + formatBytes(totalMemory) + " (Free: " + formatBytes(freeMemory) + ")");
        System.out.println("Storage Type: " + storageType);
        System.out.println("Disk Space: " + formatBytes(totalDiskSpace) + " (Free: " + formatBytes(freeDiskSpace) + ")");
        System.out.println("Java Version: " + System.getProperty("java.version"));
    }

    private static String formatBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log10(bytes) / Math.log10(1024));
        double adjustedSize = bytes / Math.pow(1024, unitIndex);
        return String.format("%.2f %s", adjustedSize, units[unitIndex]);
    }
}
