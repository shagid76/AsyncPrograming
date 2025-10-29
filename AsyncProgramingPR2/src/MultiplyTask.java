import java.util.concurrent.Callable;

public class MultiplyTask implements Callable<SegmentResult> {
    private final int[] array;
    private final int start;
    private final int end;
    private final int multiplier;

    public MultiplyTask(int[] array, int start, int end, int multiplier) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.multiplier = multiplier;
    }

    @Override
    public SegmentResult call() {
        int[] result = new int[end - start];
        for (int i = 0; i < result.length; i++) {
            result[i] = array[start + i] * multiplier;
        }
        return new SegmentResult(start, result);
    }
}