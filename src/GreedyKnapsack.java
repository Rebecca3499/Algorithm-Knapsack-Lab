import java.io.*;
import java.nio.file.*;
import java.util.*;

// 贪心：按 价值/重量 比值降序，依次尝试放入
public class GreedyKnapsack {

    static class Item {
        int id, weight, value;
        double ratio;   

        Item(int id, int w, int v) {
            this.id = id;
            this.weight = w;
            this.value = v;
            this.ratio = (double) v / w;
        }
    }

    // 入：重量数组、价值数组、容量
    // 出：Result(总价值, 选中物品列表)
    public static Result solve(int[] w, int[] v, int capacity) {
        int n = w.length;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            items.add(new Item(i, w[i], v[i]));
        }
        items.sort((a, b) -> Double.compare(b.ratio, a.ratio));

        int totalValue = 0;
        int totalWeight = 0;
        boolean[] selected = new boolean[n];

        for (Item item : items) {
            if (totalWeight + item.weight <= capacity) {
                selected[item.id] = true;
                totalWeight += item.weight;
                totalValue += item.value;
            }
        }

        return new Result(totalValue, selected);
    }

    static class Result {
        int totalValue;
        boolean[] selected;

        Result(int v, boolean[] s) { this.totalValue = v; this.selected = s; }
    }
}
