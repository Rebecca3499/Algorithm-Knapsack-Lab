import java.util.*;

// DP：一维滚动数组，O(nW) 时间，O(W) 空间
public class DPKnapsack {

    // 入：重量数组、价值数组、容量
    // 出：Result(最优总价值, 选中物品列表)
    public static Result solve(int[] wt, int[] val, int capacity) {
        int n = wt.length;
        int[] dp = new int[capacity + 1];
        // keep[i][w] = 容量w时，物品i是否被选中（用于回溯）
        boolean[][] keep = new boolean[n][capacity + 1];

        for (int i = 0; i < n; i++) {
            for (int w = capacity; w >= wt[i]; w--) {
                int take = dp[w - wt[i]] + val[i];
                if (take > dp[w]) {
                    dp[w] = take;
                    keep[i][w] = true;
                }
            }
        }

        // 回溯找出选中的物品
        boolean[] selected = new boolean[n];
        int w = capacity;
        for (int i = n - 1; i >= 0; i--) {
            if (keep[i][w]) {
                selected[i] = true;
                w -= wt[i];
            }
        }

        return new Result(dp[capacity], selected);
    }

    // 只求最优值，不回溯解——省掉 keep[][]，适合 W 很大的场景
    public static int solveValueOnly(int[] wt, int[] val, int capacity) {
        int[] dp = new int[capacity + 1];
        for (int i = 0; i < wt.length; i++) {
            for (int w = capacity; w >= wt[i]; w--) {
                int take = dp[w - wt[i]] + val[i];
                if (take > dp[w]) dp[w] = take;
            }
        }
        return dp[capacity];
    }

    static class Result {
        int totalValue;
        boolean[] selected;

        Result(int v, boolean[] s) { this.totalValue = v; this.selected = s; }
    }
}
