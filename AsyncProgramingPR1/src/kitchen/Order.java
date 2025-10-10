package kitchen;

/**
 * Модель Order — представляє одне замовлення клієнта.
 */
public class Order {
    private final int id;
    private final String dish;
    private final long placedAtMillis;

    public Order(int id, String dish) {
        this.id = id;
        this.dish = dish;
        this.placedAtMillis = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getDish() {
        return dish;
    }

    public long getPlacedAtMillis() {
        return placedAtMillis;
    }

    @Override
    public String toString() {
        return "Замовлення №" + id + ": " + dish;
    }
}