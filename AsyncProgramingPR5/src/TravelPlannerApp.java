import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TravelPlannerApp {

    static class Transport {
        String name;
        int price;
        int time;
        int seatsAvailable = -1;

        Transport(String name, int price, int time) {
            this.name = name;
            this.price = price;
            this.time = time;
        }

        @Override
        public String toString() {
            return String.format("%s {ціна=%d, час=%d хв, місця=%s}",
                    name, price, time, seatsAvailable >= 0 ? seatsAvailable : "Н/Д");
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(6);

        try {
            CompletableFuture<Transport> trainBase =
                    fetchBaseTransport("Поїзд", 700, 300, 900, executor);

            CompletableFuture<Transport> busBase =
                    fetchBaseTransport("Автобус", 400, 360, 1200, executor);

            CompletableFuture<Transport> planeBase =
                    fetchBaseTransport("Літак", 1500, 120, 1500, executor)
                            .exceptionally(ex -> {
                                System.out.println(">>> Не вдалося отримати дані для літака: " + ex.getMessage());
                                return new Transport("Літак (недоступний)",
                                        Integer.MAX_VALUE / 4,
                                        Integer.MAX_VALUE / 4);
                            });

            CompletableFuture<Integer> trainSeatsFuture =
                    fetchSeats("Поїзд", 500, executor);

            CompletableFuture<Transport> trainWithSeats =
                    trainBase.thenCombine(trainSeatsFuture, (t, seats) -> {
                        t.seatsAvailable = seats;
                        return t;
                    });

            CompletableFuture<Integer> busSeatsFuture =
                    fetchSeats("Автобус", 300, executor);

            CompletableFuture<Transport> busWithSeats =
                    busBase.thenCombine(busSeatsFuture, (t, seats) -> {
                        t.seatsAvailable = seats;
                        return t;
                    });

            CompletableFuture<Integer> planeSeatsFuture =
                    fetchSeats("Літак", 700, executor)
                            .exceptionally(ex -> {
                                System.out.println(">>> Не вдалося перевірити місця для літака: " + ex.getMessage());
                                return 0;
                            });

            CompletableFuture<Transport> planeWithSeats =
                    planeBase.thenCombine(planeSeatsFuture, (t, seats) -> {
                        t.seatsAvailable = seats;
                        return t;
                    });

            CompletableFuture<Transport> trainFinal =
                    trainWithSeats.thenCompose(t -> applyDiscountAsync(t, executor));

            CompletableFuture<Transport> busFinal =
                    busWithSeats.thenCompose(t -> applyDiscountAsync(t, executor));

            CompletableFuture<Transport> planeFinal =
                    planeWithSeats.thenCompose(t -> applyDiscountAsync(t, executor));

            CompletableFuture<Object> firstCompleted =
                    CompletableFuture.anyOf(trainFinal, busFinal, planeFinal);

            firstCompleted.thenAccept(obj -> {
                Transport first = (Transport) obj;
                System.out.println(">>> Перший отриманий результат: " + first);
            });

            CompletableFuture<Void> all =
                    CompletableFuture.allOf(trainFinal, busFinal, planeFinal);

            CompletableFuture<Transport> bestRouteFuture = all.thenApply(v -> {
                List<Transport> options = Arrays.asList(
                                trainFinal, busFinal, planeFinal
                        ).stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

                double timeWeight = 2.0 / 60.0;

                return options.stream()
                        .min(Comparator.comparingDouble(
                                t -> t.price + timeWeight * t.time
                        ))
                        .orElse(null);
            });

            Transport best = bestRouteFuture.get();

            System.out.println("\n=== Усі варіанти ===");
            System.out.println("Поїзд  : " + trainFinal.join());
            System.out.println("Автобус: " + busFinal.join());
            System.out.println("Літак  : " + planeFinal.join());

            System.out.println("\n>>> Найкращий маршрут (ціна + зважений час): " + best);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    static CompletableFuture<Transport> fetchBaseTransport(
            String name, int price, int time, int delayMs, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            sleep(delayMs);

            if ("Літак".equals(name) && delayMs > 1400) {
                throw new RuntimeException("Помилка мережі під час отримання даних літака");
            }

            System.out.println("Отримано базову інформацію для " + name +
                    " у потоці " + Thread.currentThread().getName());

            return new Transport(name, price, time);
        }, executor);
    }

    static CompletableFuture<Integer> fetchSeats(
            String name, int delayMs, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            sleep(delayMs);

            int seats = ThreadLocalRandom.current().nextInt(0, 50);

            System.out.println("Отримано кількість місць для " + name +
                    ": " + seats + " (потік " +
                    Thread.currentThread().getName() + ")");

            return seats;
        }, executor);
    }

    static CompletableFuture<Transport> applyDiscountAsync(
            Transport t, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            sleep(300);

            if (t.seatsAvailable > 10) {
                int oldPrice = t.price;
                t.price = (int) Math.round(t.price * 0.95);
                System.out.printf("Застосовано знижку для %s: %d → %d%n",
                        t.name, oldPrice, t.price);

            } else if (t.seatsAvailable == 0) {
                t.name = t.name + " (розпродано)";
                t.price = Integer.MAX_VALUE / 4;
                t.time = Integer.MAX_VALUE / 4;
                System.out.println("Немає місць → маршрут недоступний: " + t.name);

            } else {
                System.out.println("Знижка не застосована для " + t.name);
            }

            return t;
        }, executor);
    }

    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
