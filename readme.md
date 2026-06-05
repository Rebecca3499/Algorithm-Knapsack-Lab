# 代码文件说明

本项目使用 Java 实现多种算法求解 0/1 背包问题。编译命令如下：

```powershell
javac src\*.java
```

若需要显式检查 Java 8 兼容性：

```powershell
javac --release 8 src\*.java
```

## `src/GreedyKnapsack.java`

功能：实现基于价值/重量比降序排序的贪心算法。

输入参数：
- `int[] w`：物品重量数组，每个元素为正整数。
- `int[] v`：物品价值数组，每个元素为正整数。
- `int capacity`：背包容量，正整数。

输出格式：返回 `GreedyKnapsack.Result`，包含 `totalValue` 和 `selected`，分别表示总价值和物品选择方案。

## `src/DPKnapsack.java`

功能：实现一维滚动数组动态规划算法，精确求解 0/1 背包问题。

输入参数：
- `int[] wt`：物品重量数组。
- `int[] val`：物品价值数组。
- `int capacity`：背包容量。

输出格式：
- `solve(...)` 返回 `DPKnapsack.Result`，包含最优值和选择方案。
- `solveValueOnly(...)` 返回最优总价值。

## `src/BacktrackingKnapsack.java`

功能：实现带分数背包上界剪枝的回溯算法。

输入参数：
- `int[] weights`：物品重量数组。
- `int[] values`：物品价值数组。
- `int capacity`：背包容量。
- `long timeoutMs`：可选超时时间，单位为毫秒。

输出格式：返回 `BacktrackingKnapsack.Result`，包含 `totalValue`、`selected` 和 `timedOut`。

## `src/BranchBoundKnapsack.java`

功能：实现基于优先队列和分数背包上界的分支限界算法。

输入参数：
- `int[] weights`：物品重量数组。
- `int[] values`：物品价值数组。
- `int capacity`：背包容量。
- `long timeoutMs`：可选超时时间，单位为毫秒。

输出格式：返回 `BranchBoundKnapsack.Result`，包含 `totalValue`、`selected` 和 `timedOut`。

## `src/PiDataGenerator.java`

功能：生成 Pi/Pisinger 风格的 0/1 背包测试数据。

参数设置：
- 固定随机种子：`42`。
- 数据模式：`UNCORRELATED`、`WEAKLY_CORRELATED`、`STRONGLY_CORRELATED`。
- 数据规模：`S=50`、`M=500`、`L=2000`。

输出文件：写入 `data/Pi/`，每个实例包含：
- `*_w.txt`：重量数组，每行一个整数。
- `*_v.txt`：价值数组，每行一个整数。
- `*_c.txt`：背包容量，单个整数。

## `src/Experiment.java`

功能：运行 FSU 数据集实验。

运行命令：

```powershell
java -cp src Experiment
```

输入数据：`data/FSU/` 下的 `pXX_w.txt`、`pXX_p.txt`、`pXX_c.txt`、`pXX_s.txt`。

输出文件：追加写入 `experiments/result-summary.csv`，CSV 格式见“结果文件说明”。

## `src/PiExperiment.java`

功能：生成并运行 Pi 风格数据集实验。小规模实例运行四种算法；中、大规模实例跳过回溯和分支限界，避免长时间运行。

运行命令：

```powershell
java -cp src PiExperiment
```

输入数据：由 `PiDataGenerator` 生成到 `data/Pi/`。

输出文件：追加写入 `experiments/result-summary.csv`。

## `src/JJExperiment.java`

功能：运行 JJ 大规模数据集实验。默认运行贪心和动态规划，回溯和分支限界默认记为 `SKIP`。

运行命令：

```powershell
java -cp src JJExperiment
```

若需要强制尝试精确搜索，可使用：

```powershell
java -cp src JJExperiment --exact --solver=branchbound --start=1 --max=1 --timeout-ms=600000
java -cp src JJExperiment --exact --solver=backtracking --start=1 --max=1 --timeout-ms=600000
```

参数说明：
- `--start=N`：从第 N 个 JJ 实例开始运行。
- `--max=N`：最多运行 N 个 JJ 实例。
- `--solver=branchbound`：只运行分支限界。
- `--solver=backtracking`：只运行回溯。
- `--solver=both`：运行两种精确搜索。
- `--timeout-ms=N`：每个精确算法的超时时间，单位为毫秒。

输入数据：`data/JJ/problemInstances/` 和 `data/JJ/optima.csv`。若 `data/JJ` 不存在，请先运行 `setup_jj.sh` 下载数据。

输出文件：追加写入 `experiments/result-summary.csv`。

## `src/ExperimentCsvWriter.java`

功能：统一写入实验结果 CSV 文件。

输出文件：`experiments/result-summary.csv`。

# 数据文件说明

## `data/FSU/`

来源：Florida State University 0/1 背包基准数据。

文件格式：
- `pXX_c.txt`：背包容量，单个整数。
- `pXX_w.txt`：物品重量，每行一个整数。
- `pXX_p.txt`：物品价值，每行一个整数。
- `pXX_s.txt`：官方最优选择方案，每行 `0` 或 `1`。

规模参数：共 8 个实例，物品数量从 5 到 24，容量从 26 到 6,404,180。

## `data/Pi/`

来源：项目内 `PiDataGenerator` 使用固定随机种子生成。

文件格式：
- `*_c.txt`：背包容量。
- `*_w.txt`：重量数组。
- `*_v.txt`：价值数组。

规模参数：
- `S`：50 个物品。
- `M`：500 个物品。
- `L`：2000 个物品。

数据类型：
- `UNCORRELATED`：重量和价值独立随机。
- `WEAKLY_CORRELATED`：价值与重量弱相关。
- `STRONGLY_CORRELATED`：价值与重量强相关。

## `data/JJ/`

来源：Jorik Jooken 提供的 knapsackProblemInstances 数据集，由 `setup_jj.sh` 下载指定 6 个实例。

下载命令：

```powershell
& 'C:\Program Files\Git\bin\bash.exe' -lc 'cd /d/Algorithm-Knapsack-Lab && bash setup_jj.sh'
```

文件格式：
- `data/JJ/optima.csv`：实例最优值表。
- `data/JJ/problemInstances/*/test.in`：实例文件，首行为物品数，随后每行包含物品编号、价值、重量，末行为容量。

规模参数：当前选取 6 个实例，物品数量为 400 或 1000，容量为 1,000,000。

# 结果文件说明

## `experiments/result-summary.csv`

使用的应用程序：
- `Experiment`
- `PiExperiment`
- `JJExperiment`

调用的主要函数：
- `GreedyKnapsack.solve(...)`
- `DPKnapsack.solve(...)`
- `DPKnapsack.solveValueOnly(...)`
- `BacktrackingKnapsack.solveWithTimeout(...)`
- `BranchBoundKnapsack.solveWithTimeout(...)`

参数设置：
- Pi 数据固定随机种子为 `42`。
- Pi 中回溯和分支限界仅运行 `n <= 50` 的实例。
- JJ 中回溯和分支限界默认跳过；强制运行时可通过 `--timeout-ms` 设置超时时间。

文件格式：CSV，字段顺序如下：

```text
dataset_source,dataset_name,n,capacity,algorithm,value,optimum,gap_percent,time_ms,status
```

字段说明：
- `dataset_source`：数据来源，取值为 `FSU`、`Pi` 或 `JJ`。
- `dataset_name`：实例名称。
- `n`：物品数量。
- `capacity`：背包容量。
- `algorithm`：算法名称。
- `value`：算法得到的价值。
- `optimum`：用于对比的最优值。
- `gap_percent`：相对最优值的偏差百分比。
- `time_ms`：运行时间，单位毫秒。
- `status`：运行状态，取值为 `OK`、`SKIP` 或 `TIMEOUT`。

注意：该文件采用追加写入。若要重新生成干净结果，请先删除旧文件：

```powershell
Remove-Item experiments\result-summary.csv
```

然后重新运行实验入口。

## `experiments/*-knapsack01-*.txt`

为贴近老师建议的“算法名称 + 问题名称 + 数据源 + 序号 + 参数值”命名方式，程序还会为每条实验记录输出一个单独结果文件。

命名格式：

```text
算法-knapsack01-数据源-实例-c容量.txt
```

示例：

```text
dp-knapsack01-fsu-p01-c165.txt
greedy-knapsack01-pi-uncorrelated-s-c12181.txt
branchbound-knapsack01-jj-n-400-c-1m-f-0-1-c1000000.txt
```

文件格式：纯文本键值对，包含 `dataset_source`、`dataset_name`、`n`、`capacity`、`algorithm`、`value`、`optimum`、`gap_percent`、`time_ms`、`status`。

这些单独结果文件会按同名覆盖写入，不会重复追加。

# 提交结构说明

提交前请将根目录重命名为：

```text
3024244487-任务序号-2
```

若老师确认本题任务序号为 `1`，则可命名为：

```text
3024244487-1-2
```

根目录应包含：
- `src/`
- `data/`
- `experiments/`
- `reports/`
- `readme.md`
- `member.txt`
