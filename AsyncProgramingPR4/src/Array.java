import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Array {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<int[]> generatedArray = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();

            int[] arr = new Random().ints(10, 1, 20).toArray();
            System.out.println("Generated array: " + Arrays.toString(arr));
            System.out.println("Generation time: " + (System.currentTimeMillis() - start) + " ms\n");

            return arr;
        });

        CompletableFuture<int[]> modifiedArray = generatedArray.thenApplyAsync(arr -> {
            long start = System.currentTimeMillis();

            int[] mod = Arrays.stream(arr).map(x -> x + 5).toArray();
            System.out.println("Modified array (+5): " + Arrays.toString(mod));
            System.out.println("Modification time: " + (System.currentTimeMillis() - start) + " ms\n");

            return mod;
        });

        CompletableFuture<BigInteger> factorialFuture = modifiedArray.thenApplyAsync(mod -> {
            long start = System.currentTimeMillis();

            int sumOriginal = Arrays.stream(generatedArray.join()).sum();
            int sumModified = Arrays.stream(mod).sum();
            int total = sumOriginal + sumModified;

            BigInteger factorial = factorialBig(total);

            System.out.println("Sum(original) + Sum(modified) = " + total);
            System.out.println("Factorial result (BigInteger): " + factorial);
            System.out.println("Factorial computation time: " + (System.currentTimeMillis() - start) + " ms\n");

            return factorial;
        });

        CompletableFuture<Void> printFinal = factorialFuture.thenAcceptAsync(res -> {
            System.out.println("Final async output: factorial = " + res);
        }).thenRunAsync(() -> {
            System.out.println("All async operations finished.");
        });

        printFinal.get();
    }

    private static BigInteger factorialBig(int n) {
        BigInteger f = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            f = f.multiply(BigInteger.valueOf(i));
        }
        return f;
    }
}