#!/bin/bash
# 下载 JJ 数据集中选定的 6 个实例 + optima.csv
# 用法: bash setup_jj.sh

set -e

REPO="https://github.com/JorikJooken/knapsackProblemInstances.git"
TARGET="data/JJ"
INSTANCES=(
    "problemInstances/n_400_c_1000000_g_10_f_0.1_eps_0_s_100"
    "problemInstances/n_400_c_1000000_g_10_f_0.2_eps_0_s_100"
    "problemInstances/n_400_c_1000000_g_10_f_0.3_eps_0_s_100"
    "problemInstances/n_1000_c_1000000_g_10_f_0.1_eps_0_s_100"
    "problemInstances/n_1000_c_1000000_g_10_f_0.2_eps_0_s_100"
    "problemInstances/n_1000_c_1000000_g_10_f_0.3_eps_0_s_100"
)

echo "=== Cloning JJ repo (sparse, shallow) ==="
rm -rf "$TARGET" /tmp/jj-sparse
git clone --depth=1 --filter=blob:none --sparse "$REPO" /tmp/jj-sparse
cd /tmp/jj-sparse

echo "=== Checking out selected instances ==="
git sparse-checkout set --skip-checks optima.csv "${INSTANCES[@]}"

echo "=== Copying to $TARGET ==="
mkdir -p "$OLDPWD/$TARGET/problemInstances"
cp optima.csv "$OLDPWD/$TARGET/"
for d in "${INSTANCES[@]}"; do
    cp -r "$d" "$OLDPWD/$TARGET/$d"
done

cd "$OLDPWD"
rm -rf /tmp/jj-sparse
echo "=== Done: $TARGET ready ==="
