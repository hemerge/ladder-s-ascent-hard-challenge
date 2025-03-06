public class MinMaxJNI {
    static {
        System.loadLibrary("MinMaxJNI");
    }

    // Update return type to double[]
    public native double[] findMinMax(String directory);

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java MinMaxJNI <directory_path>");
            System.exit(1);
        }

        MinMaxJNI minMaxJNI = new MinMaxJNI();
        double[] result = minMaxJNI.findMinMax(args[0]);

        if (result == null || result.length < 3) {
            System.err.println("Error: JNI returned invalid results.");
            System.exit(1);
        }

        System.out.printf("Min: %.0f\n", result[0]);  // Format min/max as whole numbers
        System.out.printf("Max: %.0f\n", result[1]);
        System.out.printf("Execution Time: %.6f seconds\n", result[2]);  // Print time with decimals
    }
}
