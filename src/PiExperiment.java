import java.nio.file.*;

// Run Pi generated data. Exact tree-search solvers are limited to small instances.
public class PiExperiment {

    private static final int EXACT_SEARCH_LIMIT = 50;

    public static void main(String[] args) throws Exception {
        PiDataGenerator.main(args);

        String dir = "data/Pi";
        String[] instances = {
            "UNCORRELATED_S", "UNCORRELATED_M", "UNCORRELATED_L",
            "WEAKLY_CORRELATED_S", "WEAKLY_CORRELATED_M", "WEAKLY_CORRELATED_L",
            "STRONGLY_CORRELATED_S", "STRONGLY_CORRELATED_M", "STRONGLY_CORRELATED_L"
        };

        System.out.printf("%-45s %-6s %-12s %-12s %-12s %-12s %-12s %-10s\n",
                "Instance", "n", "Capacity", "Greedy", "DP", "Backtrack", "BranchBound", "Gap(%)");
        System.out.println(line(125));

        for (String inst : instances) {
            int capacity = Integer.parseInt(readFile(dir + "/" + inst + "_c.txt").get(0).trim());
            int[] wt = readInts(dir + "/" + inst + "_w.txt");
            int[] val = readInts(dir + "/" + inst + "_v.txt");

            long t1 = System.nanoTime();
            int dpVal = DPKnapsack.solveValueOnly(wt, val, capacity);
            long t2 = System.nanoTime();

            long t3 = System.nanoTime();
            int greedyVal = GreedyKnapsack.solve(wt, val, capacity).totalValue;
            long t4 = System.nanoTime();

            Integer backtrackingVal = null;
            long backtrackingTime = 0;
            Integer branchBoundVal = null;
            long branchBoundTime = 0;

            if (wt.length <= EXACT_SEARCH_LIMIT) {
                long t5 = System.nanoTime();
                backtrackingVal = BacktrackingKnapsack.solve(wt, val, capacity).totalValue;
                long t6 = System.nanoTime();
                backtrackingTime = t6 - t5;

                long t7 = System.nanoTime();
                branchBoundVal = BranchBoundKnapsack.solve(wt, val, capacity).totalValue;
                long t8 = System.nanoTime();
                branchBoundTime = t8 - t7;
            }

            double gap = dpVal > 0 ? (double) (dpVal - greedyVal) / dpVal * 100 : 0;
            System.out.printf("%-45s %-6d %-12d %-12d %-12d %-12s %-12s %-9.2f\n",
                    inst, wt.length, capacity,
                    greedyVal, dpVal,
                    formatValue(backtrackingVal), formatValue(branchBoundVal), gap);

            System.out.printf("  Time: DP=%.2fms, Greedy=%.3fms",
                    (t2 - t1) / 1e6, (t4 - t3) / 1e6);
            if (wt.length <= EXACT_SEARCH_LIMIT) {
                System.out.printf(", Backtracking=%.3fms, BranchBound=%.3fms\n",
                        backtrackingTime / 1e6, branchBoundTime / 1e6);
            } else {
                System.out.println(", Backtracking=SKIP, BranchBound=SKIP");
            }
        }
    }

    static String formatValue(Integer value) {
        return value == null ? "SKIP" : String.valueOf(value);
    }

    static String line(int length) {
        char[] chars = new char[length];
        java.util.Arrays.fill(chars, '-');
        return new String(chars);
    }

    static int[] readInts(String path) throws Exception {
        return Files.lines(Paths.get(path))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    static java.util.List<String> readFile(String path) throws Exception {
        return Files.readAllLines(Paths.get(path));
    }
}
