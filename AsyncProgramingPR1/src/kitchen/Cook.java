package kitchen;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Клас Кухаря — працює у власному потоці.
 * Отримує замовлення з черги та готує їх.
 */
public class Cook implements Runnable {

    private final String name;
    private final KitchenManager manager;
    private final Random rnd = new Random();

    public Cook(String name, KitchenManager manager) {
        this.name = name;
        this.manager = manager;
    }

    @Override
    public void run() {
        Thread current = Thread.currentThread();
        try {
            // Працює, поки кухня відкрита або є замовлення у черзі
            while (manager.isOpen() || !manager.getOrderQueue().isEmpty()) {

                printThreadState(current);

                // Беремо замовлення з черги
                Order order = manager.getOrderQueue().poll(1, TimeUnit.SECONDS);
                if (order == null) {
                    if (!manager.isOpen() && manager.getOrderQueue().isEmpty()) {
                        System.out.printf("[%s] Немає замовлень і кухня зачинена — %s йде додому.%n", name, name);
                        break;
                    }
                    continue;
                }

                System.out.printf("[%s] Бере %s. Чекає доступу до плити...%n", name, order);
                // Отримуємо дозвіл на приготування (Semaphore)
                manager.getStove().acquire();
                try {
                    System.out.printf("[ПЛИТА] %s готує %s (вільних місць: %d/%d)%n",
                            name, order, manager.getStove().availablePermits(), manager.getStoveCapacity());

                    int cookTimeMs = 1500 + rnd.nextInt(3500);
                    for (int elapsed = 0; elapsed < cookTimeMs; elapsed += 500) {
                        System.out.printf("[%s] %s: готуємось... (%d/%d ms)%n",
                                name, order, Math.min(elapsed, cookTimeMs), cookTimeMs);
                        Thread.sleep(500);
                    }

                    System.out.printf("[Готово] %s готове! (приготував: %s)%n", order, name);
                } finally {
                    // Звільняємо місце на плиті
                    manager.getStove().release();
                    System.out.printf("[ПЛИТА] Вивільнено місце. Вільних місць: %d/%d%n",
                            manager.getStove().availablePermits(), manager.getStoveCapacity());
                }
            }
        } catch (InterruptedException e) {
            System.err.printf("[%s] Була перервана робота кухаря.%n", name);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.printf("[%s] Помилка: %s%n", name, ex.getMessage());
        } finally {
            System.out.printf("[%s] Потік %s завершується.%n", name, Thread.currentThread().getName());
        }
    }

    // Виводить поточний стан потоку
    private void printThreadState(Thread t) {
        Thread.State state = t.getState();
        System.out.printf("[СТАН] %s — поточний стан: %s%n", t.getName(), state);
    }
}