import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArrayProcessor {
    private final int[] array;
    private final int multiplier;
    private final int numThreads;

    public ArrayProcessor(int[] array, int multiplier, int numThreads) {
        this.array = array;
        this.multiplier = multiplier;
        this.numThreads = numThreads;
    }

    public CopyOnWriteArrayList<Integer> processArray() {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<SegmentResult>> futures = new ArrayList<>();

        int size = array.length;
        int chunkSize = (int) Math.ceil((double) size / numThreads);

        for (int i = 0; i < size; i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, size);
            futures.add(executor.submit(new MultiplyTask(array, start, end, multiplier)));
        }

        CopyOnWriteArrayList<Integer> result = new CopyOnWriteArrayList<>(Collections.nCopies(size, 0));

        for (Future<SegmentResult> future : futures) {
            try {
                SegmentResult seg = future.get();
                int startIdx = seg.getStartIndex();
                int[] vals = seg.getValues();
                for (int j = 0; j < vals.length; j++) {
                    result.set(startIdx + j, vals[j]);
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Помилка у потоці: " + e.getMessage());
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }
}