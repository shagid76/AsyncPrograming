import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Введіть кількість елементів (40–60): ");
        int size = sc.nextInt();
        size = Math.max(40, Math.min(60, size));

        System.out.print("Введіть множник: ");
        int multiplier = sc.nextInt();

        Random rnd = new Random();
        int[] array = rnd.ints(size, -100, 101).toArray();

        System.out.println("\nПочатковий масив:");
        System.out.println(Arrays.toString(array));

        int numThreads = 4;
        long startTime = System.currentTimeMillis();

        ArrayProcessor processor = new ArrayProcessor(array, multiplier, numThreads);
        CopyOnWriteArrayList<Integer> result = processor.processArray();

        long endTime = System.currentTimeMillis();

        System.out.println("\nРезультуючий масив:");
        System.out.println(result);
        System.out.println("\nЧас виконання: " + (endTime - startTime) + " мс");
    }
}