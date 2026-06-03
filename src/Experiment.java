import java.nio.file.*;

// Run FSU data with Greedy, DP, Backtracking, and Branch-and-Bound.
public class Experiment {

    public static void main(String[] args) throws Exception {
        String dataDir = "data/FSU";
        String[] problems = {"p01", "p02", "p03", "p04", "p05", "p06", "p07", "p08"};
        String expDir = "experiments";
        Files.createDirectories(Paths.get(expDir));

        System.out.printf("%-6s %-6s %-10s %-10s %-10s %-12s %-12s %-10s %-10s\n",
                "Problem", "n", "Capacity", "Greedy", "DP", "Backtrack", "BranchBound", "Optimal", "Gap(%)");
        System.out.println(line(105));

        for (String p : problems) {
            int capacity = Integer.parseInt(readFile(dataDir + "/" + p + "_c.txt").get(0).trim());
            int[] wt = readInts(dataDir + "/" + p + "_w.txt");
            int[] val = readInts(dataDir + "/" + p + "_p.txt");
            int[] optSel = readInts(dataDir + "/" + p + "_s.txt");

            int optVal = 0;
            for (int i = 0; i < optSel.length; i++) {
                if (optSel[i] == 1) {
                    optVal += val[i];
                }
            }

            long t1 = System.nanoTime();
            DPKnapsack.Result dpRes = DPKnapsack.solve(wt, val, capacity);
            long t2 = System.nanoTime();

            long t3 = System.nanoTime();
            GreedyKnapsack.Result greedyRes = GreedyKnapsack.solve(wt, val, capacity);
            long t4 = System.nanoTime();

            long t5 = System.nanoTime();
            BacktrackingKnapsack.Result backtrackingRes = BacktrackingKnapsack.solve(wt, val, capacity);
            long t6 = System.nanoTime();

            long t7 = System.nanoTime();
            BranchBoundKnapsack.Result branchBoundRes = BranchBoundKnapsack.solve(wt, val, capacity);
            long t8 = System.nanoTime();

            double gap = (double) (optVal - greedyRes.totalValue) / optVal * 100;
            System.out.printf("%-6s %-6d %-10d %-10d %-10d %-12d %-12d %-10d %-9.2f\n",
                    p.toUpperCase(), wt.length, capacity,
                    greedyRes.totalValue, dpRes.totalValue,
                    backtrackingRes.totalValue, branchBoundRes.totalValue,
                    optVal, gap);

            warnIfMismatch("DP", dpRes.totalValue, optVal);
            warnIfMismatch("Backtracking", backtrackingRes.totalValue, optVal);
            warnIfMismatch("BranchBound", branchBoundRes.totalValue, optVal);

            System.out.printf("  Time: DP=%.3fms, Greedy=%.3fms, Backtracking=%.3fms, BranchBound=%.3fms\n",
                    (t2 - t1) / 1e6, (t4 - t3) / 1e6, (t6 - t5) / 1e6, (t8 - t7) / 1e6);
        }
    }

    static void warnIfMismatch(String algorithm, int actual, int expected) {
        if (actual != expected) {
            System.out.println("  [WARN] " + algorithm + " result " + actual + " != optimal " + expected);
        }
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
