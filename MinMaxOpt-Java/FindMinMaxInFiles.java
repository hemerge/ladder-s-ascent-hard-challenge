import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FindMinMaxInFiles {
    private static final String FOLDER_PATH = "challenge-1";

    // Load the native library
    static {
        System.loadLibrary("findminmax");
    }

    // Declare native method (Implemented in C)
    private native long[] findMinMaxInC(String folderPath);

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        // Call the C function directly for min/max computation
        FindMinMaxInFiles finder = new FindMinMaxInFiles();
        long[] result = finder.findMinMaxInC(FOLDER_PATH);

        long globalMin = result[0];
        long globalMax = result[1];

        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1e9;

        System.out.println("Processing Complete!");
        System.out.println("Global Min: " + globalMin);
        System.out.println("Global Max: " + globalMax);
        System.out.printf("Time Taken: %.4f seconds%n", elapsedTime);
    }
}
