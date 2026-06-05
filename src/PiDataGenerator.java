import java.io.*;
import java.nio.file.*;
import java.util.*;

// Pi 风格数据生成器：模拟不同特征的 0/1 背包实例
// 参考 Pisinger 论文中的实例类型
public class PiDataGenerator {

    static final long SEED = 42L;
    static Random rand = new Random(SEED); // 固定种子，可复现

    // 生成模式
    enum Mode {
        UNCORRELATED,      // 重量、价值各自独立随机
        WEAKLY_CORRELATED, // 价值 ≈ 重量 + 小随机偏移
        STRONGLY_CORRELATED // 价值 = 重量 + 固定常数
    }

    static void generate(String name, Mode mode, int n, int maxW) throws Exception {
        int[] wt = new int[n];
        int[] val = new int[n];

        for (int i = 0; i < n; i++) {
            wt[i] = rand.nextInt(maxW) + 1;
            switch (mode) {
                case UNCORRELATED:
                    val[i] = rand.nextInt(maxW) + 1;
                    break;
                case WEAKLY_CORRELATED:
                    val[i] = wt[i] + rand.nextInt(maxW / 4) - maxW / 8;
                    if (val[i] <= 0) val[i] = 1;
                    break;
                case STRONGLY_CORRELATED:
                    val[i] = wt[i] + maxW / 10;
                    break;
            }
        }

        // 容量设为总重量的一半（经典做法）
        long sumW = 0;
        for (int w : wt) sumW += w;
        int capacity = (int) (sumW / 2);

        String dir = "data/Pi";
        Files.createDirectories(Paths.get(dir));
        writeInts(dir + "/" + name + "_w.txt", wt);
        writeInts(dir + "/" + name + "_v.txt", val);
        writeFile(dir + "/" + name + "_c.txt", String.valueOf(capacity));
        System.out.printf("  %-40s n=%-6d capacity=%-10d\n", name, n, capacity);
    }

    static void writeInts(String path, int[] arr) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int x : arr) sb.append(x).append("\n");
        writeFile(path, sb.toString());
    }

    static void writeFile(String path, String content) throws Exception {
        Files.write(Paths.get(path), content.getBytes());
    }

    public static void main(String[] args) throws Exception {
        rand = new Random(SEED);
        System.out.println("Generating Pi-style instances...\n");

        // 三种模式 × 三种规模
        for (Mode mode : Mode.values()) {
            System.out.println(mode + ":");
            generate(mode + "_S",  mode, 50,   1000);
            generate(mode + "_M",  mode, 500,  5000);
            generate(mode + "_L",  mode, 2000, 5000);
        }

        System.out.println("\nDone. Files in data/Pi/");
    }
}
