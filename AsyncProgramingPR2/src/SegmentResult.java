public class SegmentResult {
    private final int startIndex;
    private final int[] values;

    public SegmentResult(int startIndex, int[] values) {
        this.startIndex = startIndex;
        this.values = values;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int[] getValues() {
        return values;
    }
}