import java.util.*;

// Exact 0/1 knapsack solver using best-first branch and bound.
public class BranchBoundKnapsack {

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

    private static class Node {
        int level;
        int weight;
        int value;
        double bound;
        boolean[] selected;

        Node(int level, int weight, int value, boolean[] selected) {
            this.level = level;
            this.weight = weight;
            this.value = value;
            this.selected = selected;
        }
    }

    static class Result {
        int totalValue;
        boolean[] selected;

        Result(int totalValue, boolean[] selected) {
            this.totalValue = totalValue;
            this.selected = selected;
        }
    }

    public static Result solve(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        Item[] items = new Item[n];
        for (int i = 0; i < n; i++) {
            items[i] = new Item(i, weights[i], values[i]);
        }
        Arrays.sort(items, (a, b) -> Double.compare(b.ratio, a.ratio));

        GreedyKnapsack.Result greedy = GreedyKnapsack.solve(weights, values, capacity);
        int bestValue = greedy.totalValue;
        boolean[] bestSelected = greedy.selected.clone();

        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> Double.compare(b.bound, a.bound));
        Node root = new Node(0, 0, 0, new boolean[n]);
        root.bound = bound(root, items, capacity);
        pq.add(root);

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            if (node.bound <= bestValue || node.level >= n) {
                continue;
            }

            Item item = items[node.level];

            boolean[] withSelected = node.selected.clone();
            withSelected[item.id] = true;
            Node with = new Node(
                    node.level + 1,
                    node.weight + item.weight,
                    node.value + item.value,
                    withSelected);
            if (with.weight <= capacity) {
                if (with.value > bestValue) {
                    bestValue = with.value;
                    bestSelected = with.selected.clone();
                }
                with.bound = bound(with, items, capacity);
                if (with.bound > bestValue) {
                    pq.add(with);
                }
            }

            Node without = new Node(
                    node.level + 1,
                    node.weight,
                    node.value,
                    node.selected.clone());
            without.bound = bound(without, items, capacity);
            if (without.bound > bestValue) {
                pq.add(without);
            }
        }

        return new Result(bestValue, bestSelected);
    }

    private static double bound(Node node, Item[] items, int capacity) {
        if (node.weight >= capacity) {
            return node.value;
        }

        double result = node.value;
        int totalWeight = node.weight;

        for (int i = node.level; i < items.length; i++) {
            Item item = items[i];
            if (totalWeight + item.weight <= capacity) {
                totalWeight += item.weight;
                result += item.value;
            } else {
                int remaining = capacity - totalWeight;
                if (remaining > 0) {
                    result += item.ratio * remaining;
                }
                break;
            }
        }

        return result;
    }
}
