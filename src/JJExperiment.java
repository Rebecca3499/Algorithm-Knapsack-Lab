import java.nio.file.*;

// Run JJ data with Greedy and DP. Exact tree-search solvers are skipped for n >= 400.
public class JJExperiment {

    public static void main(String[] args) throws Exception {
        boolean runExact = hasArg(args, "--exact");
        String solver = getStringArg(args, "--solver", "both");
        int startInstance = Math.max(1, getIntArg(args, "--start", 1));
        int maxInstances = getIntArg(args, "--max", Integer.MAX_VALUE);
        long exactTimeoutMs = getLongArg(args, "--timeout-ms", 60_000L);

        String base = "data/JJ/problemInstances";
        String optimaPath = "data/JJ/optima.csv";

        if (!Files.isDirectory(Paths.get(base)) || !Files.exists(Paths.get(optimaPath))) {
            System.out.println("JJ data is missing. Run setup_jj.sh first, then rerun JJExperiment.");
            System.out.println("Windows Git Bash example:");
            System.out.println("  \"C:\\Program Files\\Git\\bin\\bash.exe\" -lc 'cd /d/Algorithm-Knapsack-Lab && bash setup_jj.sh'");
            return;
        }

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
            String backtrackingStatus = "SKIP";
            String branchBoundStatus = "SKIP";
            long backtrackingTime = 0;
            long branchBoundTime = 0;
            String exactTime = "";
            if (runExact) {
                System.out.println("  Running exact solvers for " + shortName + " ...");

                StringBuilder exactTimeBuilder = new StringBuilder("  Exact:");
                if ("both".equals(solver) || "branchbound".equals(solver)) {
                    long t5 = System.nanoTime();
                    BranchBoundKnapsack.Result branchBoundRes =
                            BranchBoundKnapsack.solveWithTimeout(wt, val, cap, exactTimeoutMs);
                    long t6 = System.nanoTime();
                    branchBoundTime = t6 - t5;
                    branchBoundValue = String.valueOf(branchBoundRes.totalValue);
                    branchBoundStatus = branchBoundRes.timedOut ? "TIMEOUT" : "OK";
                    exactTimeBuilder.append(String.format(" BranchBound=%.2fms(%s)",
                            branchBoundTime / 1e6, branchBoundStatus));
                }

                if ("both".equals(solver) || "backtracking".equals(solver)) {
                    long t7 = System.nanoTime();
                    BacktrackingKnapsack.Result backtrackingRes =
                            BacktrackingKnapsack.solveWithTimeout(wt, val, cap, exactTimeoutMs);
                    long t8 = System.nanoTime();
                    backtrackingTime = t8 - t7;
                    backtrackingValue = String.valueOf(backtrackingRes.totalValue);
                    backtrackingStatus = backtrackingRes.timedOut ? "TIMEOUT" : "OK";
                    exactTimeBuilder.append(String.format(" Backtracking=%.2fms(%s)",
                            backtrackingTime / 1e6, backtrackingStatus));
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

            writeCsvRow(shortName, n, cap, "DP", dpVal, bestVal, t3, t4, "OK");
            writeCsvRow(shortName, n, cap, "Greedy", greedyVal, bestVal, t1, t2, "OK");
            writeExactCsvRow(shortName, n, cap, "Backtracking", backtrackingValue,
                    bestVal, backtrackingTime, backtrackingStatus);
            writeExactCsvRow(shortName, n, cap, "BranchBound", branchBoundValue,
                    bestVal, branchBoundTime, branchBoundStatus);
        }

        if (!runExact) {
            System.out.println("\nNote: JJ instances here have n=400/1000, so exact Backtracking and BranchBound are skipped by default.");
            System.out.println("      Add --exact to run them anyway, and use --start=N/--max=N to choose instances.");
            System.out.println("      Add --solver=backtracking or --solver=branchbound to try one exact solver at a time.");
            System.out.println("      Add --timeout-ms=N to control each exact solver timeout.");
        }
    }

    static void writeCsvRow(
            String name,
            int n,
            int capacity,
            String algorithm,
            int value,
            int optimum,
            long startNanos,
            long endNanos,
            String status) throws Exception {
        ExperimentCsvWriter.write(
                "JJ",
                name,
                n,
                capacity,
                algorithm,
                ExperimentCsvWriter.value(value),
                ExperimentCsvWriter.value(optimum),
                ExperimentCsvWriter.gapPercent(value, optimum),
                ExperimentCsvWriter.timeMs(startNanos, endNanos),
                status);
    }

    static void writeExactCsvRow(
            String name,
            int n,
            int capacity,
            String algorithm,
            String value,
            int optimum,
            long elapsedNanos,
            String status) throws Exception {
        boolean hasValue = value != null && !"SKIP".equals(value);
        ExperimentCsvWriter.write(
                "JJ",
                name,
                n,
                capacity,
                algorithm,
                hasValue ? value : ExperimentCsvWriter.empty(),
                ExperimentCsvWriter.value(optimum),
                hasValue ? ExperimentCsvWriter.gapPercent(Integer.parseInt(value), optimum) : ExperimentCsvWriter.empty(),
                String.format(java.util.Locale.US, "%.3f", elapsedNanos / 1e6),
                status);
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

    static long getLongArg(String[] args, String name, long defaultValue) {
        String prefix = name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return Long.parseLong(arg.substring(prefix.length()));
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
