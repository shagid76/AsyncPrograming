package kitchen;

/**
 * Головний клас
 */
public class KitchenSimulation {
    private static final int NUM_COOKS = 3;
    private static final int STOVE_CAPACITY = 4;
    private static final int OPEN_SECONDS = 5;

    public static void main(String[] args) {
        System.out.println("=== Симуляція кухні ===");

        KitchenManager manager = new KitchenManager(NUM_COOKS, STOVE_CAPACITY);
        manager.openKitchen();
        manager.generateOrders(OPEN_SECONDS);
        manager.waitUntilFinished();

        System.out.println("=== Симуляція завершена ===");
    }
}