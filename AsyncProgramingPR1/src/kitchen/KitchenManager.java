package kitchen;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Керує кухнею: чергою замовлень, плитою (Semaphore),
 * запуском кухарів і закриттям кухні.
 */
public class KitchenManager {

    private final int stoveCapacity;
    private final Semaphore stove;
    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private final List<Thread> cooks = new ArrayList<>();
    private final AtomicInteger orderCounter = new AtomicInteger(0);
    private volatile boolean isOpen = true;

    private final String[] MENU = {
            "Салат Цезар", "Борщ", "Стейк", "Плов",
            "Паста Карбонара", "Риба з овочами", "Омлет", "Піца Маргарита"
    };

    // --- Гетери для доступу з Cook ---
    public BlockingQueue<Order> getOrderQueue() { return orderQueue; }
    public Semaphore getStove() { return stove; }
    public int getStoveCapacity() { return stoveCapacity; }
    public boolean isOpen() { return isOpen; }

    public KitchenManager(int numCooks, int stoveCapacity) {
        this.stoveCapacity = stoveCapacity;
        this.stove = new Semaphore(stoveCapacity);
        for (int i = 1; i <= numCooks; i++) {
            Cook cook = new Cook("Кухар-" + i, this);
            Thread thread = new Thread(cook, "CookThread-" + i);
            cooks.add(thread);
        }
    }

    // запускає всі потоки кухарів
    public void openKitchen() {
        System.out.println("=== Кухня відкрита ===");
        cooks.forEach(Thread::start);
    }

    // нові замовлення більше не приймаються
    public void closeKitchen() {
        isOpen = false;
        System.out.println("=== Кухня зачиняється. Нові замовлення не приймаються. ===");
    }

    // генерує замовлення протягом заданої кількості секунд
    public void generateOrders(int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        Random rnd = new Random();
        while (System.currentTimeMillis() < end) {
            int id = orderCounter.incrementAndGet();
            String dish = MENU[rnd.nextInt(MENU.length)];
            Order order = new Order(id, dish);
            try {
                if (orderQueue.offer(order, 1, TimeUnit.SECONDS)) {
                    System.out.printf("[Клієнт] Прийнято %s%n", order);
                }
                Thread.sleep(300 + rnd.nextInt(900));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        closeKitchen();
    }

    // очікує завершення всіх кухарів після обробки черги
    public void waitUntilFinished() {
        while (!orderQueue.isEmpty()) {
            System.out.printf("[Система] У черзі залишилось: %d замовлень.%n", orderQueue.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (Thread t : cooks) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("=== Усі кухарі завершили роботу ===");
    }
}