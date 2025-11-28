import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Task2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long startProgram = System.currentTimeMillis();

        CompletableFuture<int[]> sequenceFuture = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();

            int[] arr = new Random().ints(20, 1, 50).toArray();
            System.out.println("Generated sequence: " + Arrays.toString(arr));
            System.out.println("Generation: " + (System.currentTimeMillis() - start) + " ms\n");

            return arr;
        });

        CompletableFuture<int[]> sortedFuture = sequenceFuture.thenApplyAsync(arr -> {
            long start = System.currentTimeMillis();

            int[] sorted = arr.clone();
            Arrays.sort(sorted);

            System.out.println("Sorted sequence: " + Arrays.toString(sorted));
            System.out.println("Sorting: " + (System.currentTimeMillis() - start) + " ms\n");

            return sorted;
        });

        CompletableFuture<int[]> pairSumsFuture = sortedFuture.thenApplyAsync(sorted -> {
            long start = System.currentTimeMillis();

            int[] pairSums = new int[sorted.length - 1];
            for (int i = 0; i < sorted.length - 1; i++) {
                pairSums[i] = sorted[i] + sorted[i + 1];
            }

            System.out.println("Pair sums array: " + Arrays.toString(pairSums));
            System.out.println("Pair sums generation: " + (System.currentTimeMillis() - start) + " ms\n");

            return pairSums;
        });


        CompletableFuture<Void> finalOutput = CompletableFuture.allOf(pairSumsFuture)
                .thenRunAsync(() -> {
                    try {
                        System.out.println("===== FINAL RESULTS =====");
                        System.out.println("All async operations completed.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        finalOutput.get();

        System.out.println("\nTotal async execution time: " +
                (System.currentTimeMillis() - startProgram) + " ms");
    }
}