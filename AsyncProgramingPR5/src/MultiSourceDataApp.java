import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MultiSourceDataApp {

    public static void main(String[] args) {
        CompletableFuture<String> source1 = CompletableFuture.supplyAsync(() -> {
            sleep(2);
            return "Дані з API 1";
        });

        CompletableFuture<String> source2 = source1.thenCompose(data ->
                CompletableFuture.supplyAsync(() -> {
                    sleep(1);
                    return data + " + Дані з API 2";
                })
        );

        CompletableFuture<String> source3 = CompletableFuture.supplyAsync(() -> {
            sleep(3);
            return "Дані з API 3";
        });

        CompletableFuture<Object> first = CompletableFuture.anyOf(source1, source2, source3);
        first.thenAccept(result ->
                System.out.println("Перший отриманий результат: " + result)
        );

        CompletableFuture<Void> all = CompletableFuture.allOf(source1, source2, source3);

        all.thenRun(() -> {
            System.out.println("\nУсі дані отримано:");
            System.out.println(source1.join());
            System.out.println(source2.join());
            System.out.println(source3.join());
        }).join();
    }

    private static void sleep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}