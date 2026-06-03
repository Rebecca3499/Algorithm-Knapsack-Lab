import java.nio.file.*;

// Run JJ data with Greedy and DP. Exact tree-search solvers are skipped for n >= 400.
public class JJExperiment {

    public static void main(String[] args) throws Exception {
        boolean runExact = hasArg(args, "--exact");
        String solver = getStringArg(args, "--solver", "both");
        int startInstance = Math.max(1, getIntArg(args, "--start", 1));
        int maxInstances = getIntArg(args, "--max", Integer.MAX_VALUE);

        String base = "data/JJ/problemInstances";
        String optimaPath = "data/JJ/optima.csv";

        java.util.HashMap<String, Integer> optima = new java.util.HashMap<String, Integer>();
        for (String line : Files.readAllLines(Paths.get(optimaPath))) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                try {
                    optima.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String[] instances = {
            "n_400_c_1000000_g_10_f_0.1_eps_0_s_100",
            "n_400_c_1000000_g_10_f_0.2_eps_0_s_100",
            "n_400_c_1000000_g_10_f_0.3_eps_0_s_100",
            "n_1000_c_1000000_g_10_f_0.1_eps_0_s_100",
            "n_1000_c_1000000_g_10_f_0.2_eps_0_s_100",
            "n_1000_c_1000000_g_10_f_0.3_eps_0_s_100",
        };

        System.out.printf("%-40s %-6s %-10s %-10s %-10s %-12s %-12s %-7s %-8s\n",
                "Instance", "n", "Capacity", "Greedy", "DP", "Backtrack", "BranchBound", "Gap(%)", "DP Time");
        System.out.println(line(125));

        int endInstance = Math.min(instances.length, startInstance - 1 + maxInstances);
        for (int instanceIndex = startInstance - 1; instanceIndex < endInstance; instanceIndex++) {
            String inst = instances[instanceIndex];
            String dir = base + "/" + inst;
            java.util.List<String> lines = Files.readAllLines(Paths.get(dir + "/test.in"));

            int n = Integer.parseInt(lines.get(0).trim());
            int[] wt = new int[n];
            int[] val = new int[n];
            for (int i = 0; i < n; i++) {
                String[] p = lines.get(i + 1).trim().split("\\s+");
                val[i] = Integer.parseInt(p[1]);
                wt[i] = Integer.parseInt(p[2]);
            }
            int cap = Integer.parseInt(lines.get(lines.size() - 1).trim());

            String shortName = inst.replaceAll(
                    "n_(\\d+)_c_(\\d+)_g_\\d+_f_([\\d.]+)_eps_0_s_100",
                    "n=$1,c=1M,f=$3");

            long t1 = System.nanoTime();
            int greedyVal = GreedyKnapsack.solve(wt, val, cap).totalValue;
            long t2 = System.nanoTime();

            long t3 = System.nanoTime();
            int dpVal = DPKnapsack.solveValueOnly(wt, val, cap);
            long t4 = System.nanoTime();

            String backtrackingValue = "SKIP";
            String branchBoundValue = "SKIP";
            String exactTime = "";
            if (runExact) {
                System.out.println("  Running exact solvers for " + shortName + " ...");

                StringBuilder exactTimeBuilder = new StringBuilder("  Exact:");
                if ("both".equals(solver) || "branchbound".equals(solver)) {
                    long t5 = System.nanoTime();
                    BranchBoundKnapsack.Result branchBoundRes = BranchBoundKnapsack.solve(wt, val, cap);
                    long t6 = System.nanoTime();
                    branchBoundValue = String.valueOf(branchBoundRes.totalValue);
                    exactTimeBuilder.append(String.format(" BranchBound=%.2fms", (t6 - t5) / 1e6));
                }

                if ("both".equals(solver) || "backtracking".equals(solver)) {
                    long t7 = System.nanoTime();
                    BacktrackingKnapsack.Result backtrackingRes = BacktrackingKnapsack.solve(wt, val, cap);
                    long t8 = System.nanoTime();
                    backtrackingValue = String.valueOf(backtrackingRes.totalValue);
                    exactTimeBuilder.append(String.format(" Backtracking=%.2fms", (t8 - t7) / 1e6));
                }

                exactTime = exactTimeBuilder.toString();
            }

            int bestVal = dpVal > 0 ? dpVal : optima.getOrDefault(inst, -1);
            double gap = bestVal > 0 ? (double) (bestVal - greedyVal) / bestVal * 100 : 0;

            System.out.printf("%-40s %-6d %-10d %-10d %-10d %-12s %-12s %-6.2f %-8s\n",
                    shortName, n, cap, greedyVal, bestVal,
                    backtrackingValue, branchBoundValue,
                    gap, String.format("%.0fms", (t4 - t3) / 1e6));
            System.out.printf("  Greedy: %.2fms\n", (t2 - t1) / 1e6);
            if (!exactTime.isEmpty()) {
                System.out.println(exactTime);
            }
        }

        if (!runExact) {
            System.out.println("\nNote: JJ instances here have n=400/1000, so exact Backtracking and BranchBound are skipped by default.");
            System.out.println("      Add --exact to run them anyway, and use --start=N/--max=N to choose instances.");
            System.out.println("      Add --solver=backtracking or --solver=branchbound to try one exact solver at a time.");
        }
    }

    static String line(int length) {
        char[] chars = new char[length];
        java.util.Arrays.fill(chars, '-');
        return new String(chars);
    }

    static boolean hasArg(String[] args, String target) {
        for (String arg : args) {
            if (target.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    static int getIntArg(String[] args, String name, int defaultValue) {
        String prefix = name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return Integer.parseInt(arg.substring(prefix.length()));
            }
        }
        return defaultValue;
    }

    static String getStringArg(String[] args, String name, String defaultValue) {
        String prefix = name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length()).toLowerCase();
            }
        }
        return defaultValue;
    }
}
