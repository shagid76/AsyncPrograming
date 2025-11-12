import java.util.*;
import java.util.concurrent.*;

public class PairSumParallel {

    static class PairSumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10_000;
        private final int[] arr;
        private final int start, end;

        public PairSumTask(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end - 1; i++) {
                    sum += arr[i] + arr[i + 1];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;
                PairSumTask left = new PairSumTask(arr, start, mid);
                PairSumTask right = new PairSumTask(arr, mid, end);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    // ---------- Work Dealing (ExecutorService) ----------
    static class SumWorker implements Callable<Long> {
        private final int[] arr;
        private final int start, end;

        public SumWorker(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        public Long call() {
            long sum = 0;
            for (int i = start; i < end - 1; i++) {
                sum += arr[i] + arr[i + 1];
            }
            return sum;
        }
    }

    // ---------- Безпечне введення ----------
    private static int safeIntInput(Scanner sc, String message, int minValue, boolean allowEqualMin) {
        int num;
        while (true) {
            System.out.print(message);
            try {
                num = sc.nextInt();
                if (!allowEqualMin && num <= minValue) {
                    System.out.println("❌ Значення має бути більше " + minValue + ". Спробуйте ще раз.\n");
                } else {
                    return num;
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Введено не число. Спробуйте ще раз.\n");
                sc.nextLine(); // очищає буфер
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        int n = safeIntInput(sc, "Введіть кількість елементів масиву: ", 1, false);
        int min = safeIntInput(sc, "Введіть початкове значення: ", Integer.MIN_VALUE, true);
        int max;
        while (true) {
            max = safeIntInput(sc, "Введіть кінцеве значення: ", Integer.MIN_VALUE, true);
            if (max < min) {
                System.out.println("❌ Кінцеве значення не може бути меншим за початкове.\n");
            } else break;
        }

        int[] arr = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            arr[i] = rand.nextInt(max - min + 1) + min;
        }

        System.out.println("\n✅ Згенерований масив:");
        System.out.println(Arrays.toString(arr));

        int cores = Runtime.getRuntime().availableProcessors();
        int chunk = Math.max(1, n / cores);

        long start1 = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < n - 1; i += chunk) {
            int end = Math.min(i + chunk + 1, n); // +1 щоб не втратити суму між блоками
            futures.add(executor.submit(new SumWorker(arr, i, end)));
        }

        long totalSum1 = 0;
        for (Future<Long> f : futures) totalSum1 += f.get();
        executor.shutdown();
        long end1 = System.nanoTime();

        // ---------- Work Stealing ----------
        long start2 = System.nanoTime();
        ForkJoinPool pool = new ForkJoinPool();
        long totalSum2 = pool.invoke(new PairSumTask(arr, 0, arr.length));
        long end2 = System.nanoTime();

        // ---------- Перевірка правильності ----------
        long checkSum = 0;
        for (int i = 0; i < n - 1; i++) {
            checkSum += arr[i] + arr[i + 1];
        }

        System.out.println("\nРезультат (Work Dealing): " + totalSum1);
        System.out.printf("Час виконання: %.3f мс%n", (end1 - start1) / 1_000_000.0);

        System.out.println("\nРезультат (Work Stealing): " + totalSum2);
        System.out.printf("Час виконання: %.3f мс%n", (end2 - start2) / 1_000_000.0);

        System.out.println("\nПеревірка (послідовно): " + checkSum);
        if (checkSum == totalSum1 && checkSum == totalSum2) {
            System.out.println("✅ Результати збігаються. Обчислення виконано правильно!");
        } else {
            System.out.println("❌ Результати не збігаються. Перевірте алгоритм!");
        }
    }
}