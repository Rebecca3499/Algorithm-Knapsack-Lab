import java.util.*;

// Exact 0/1 knapsack solver using depth-first backtracking with upper-bound pruning.
public class BacktrackingKnapsack {

    private static class Item {
        int id;
        int weight;
        int value;
        double ratio;

        Item(int id, int weight, int value) {
            this.id = id;
            this.weight = weight;
            this.value = value;
            this.ratio = (double) value / weight;
        }
    }

    static class Result {
        int totalValue;
        boolean[] selected;
        boolean timedOut;

        Result(int totalValue, boolean[] selected) {
            this(totalValue, selected, false);
        }

        Result(int totalValue, boolean[] selected, boolean timedOut) {
            this.totalValue = totalValue;
            this.selected = selected;
            this.timedOut = timedOut;
        }
    }

    private Item[] items;
    private int capacity;
    private int bestValue;
    private boolean[] currentSelected;
    private boolean[] bestSelected;
    private long deadlineNanos;
    private boolean timedOut;

    public static Result solve(int[] weights, int[] values, int capacity) {
        return new BacktrackingKnapsack().run(weights, values, capacity, 0);
    }

    public static Result solveWithTimeout(int[] weights, int[] values, int capacity, long timeoutMs) {
        return new BacktrackingKnapsack().run(weights, values, capacity, timeoutMs);
    }

    private Result run(int[] weights, int[] values, int capacity, long timeoutMs) {
        int n = weights.length;
        this.capacity = capacity;
        this.deadlineNanos = timeoutMs > 0 ? System.nanoTime() + timeoutMs * 1_000_000L : Long.MAX_VALUE;
        this.timedOut = false;
        this.items = new Item[n];
        for (int i = 0; i < n; i++) {
            items[i] = new Item(i, weights[i], values[i]);
        }
        Arrays.sort(items, (a, b) -> Double.compare(b.ratio, a.ratio));

        this.currentSelected = new boolean[n];
        this.bestSelected = new boolean[n];

        GreedyKnapsack.Result greedy = GreedyKnapsack.solve(weights, values, capacity);
        this.bestValue = greedy.totalValue;
        this.bestSelected = greedy.selected.clone();

        search(0, 0, 0);
        return new Result(bestValue, bestSelected, timedOut);
    }

    private void search(int index, int currentWeight, int currentValue) {
        if (timedOut || isExpired()) {
            timedOut = true;
            return;
        }
        if (currentWeight > capacity) {
            return;
        }
        if (index == items.length) {
            updateBest(currentValue);
            return;
        }
        if (upperBound(index, currentWeight, currentValue) <= bestValue) {
            return;
        }

        Item item = items[index];

        if (currentWeight + item.weight <= capacity) {
            currentSelected[item.id] = true;
            search(index + 1, currentWeight + item.weight, currentValue + item.value);
            currentSelected[item.id] = false;
        }

        search(index + 1, currentWeight, currentValue);
    }

    private boolean isExpired() {
        return deadlineNanos != Long.MAX_VALUE && System.nanoTime() >= deadlineNanos;
    }

    private void updateBest(int value) {
        if (value > bestValue) {
            bestValue = value;
            bestSelected = currentSelected.clone();
        }
    }

    private double upperBound(int index, int currentWeight, int currentValue) {
        double bound = currentValue;
        int totalWeight = currentWeight;

        for (int i = index; i < items.length; i++) {
            Item item = items[i];
            if (totalWeight + item.weight <= capacity) {
                totalWeight += item.weight;
                bound += item.value;
            } else {
                int remaining = capacity - totalWeight;
                if (remaining > 0) {
                    bound += item.ratio * remaining;
                }
                break;
            }
        }

        return bound;
    }
}
