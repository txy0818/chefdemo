#!/bin/bash

# MinIO 快速配置脚本
# 用于快速配置 chef-platform bucket 和跨域访问

echo "================================"
echo "MinIO 快速配置脚本"
echo "================================"
echo ""

# 检查 mc 是否安装
if ! command -v mc &> /dev/null; then
    echo "❌ mc 命令未找到，请先安装 MinIO Client"
    echo ""
    echo "安装方法:"
    echo "  macOS:   brew install minio/stable/mc"
    echo "  Linux:   wget https://dl.min.io/client/mc/release/linux-amd64/mc && chmod +x mc"
    echo "  Windows: 访问 https://min.io/docs/minio/windows/reference/minio-mc.html"
    echo ""
    exit 1
fi

echo "✅ mc 命令已安装"
echo ""

# MinIO 配置
MINIO_ENDPOINT="http://127.0.0.1:9000"
MINIO_USER="minioadmin"
MINIO_PASS="minioadmin"
BUCKET_NAME="chef-platform"
ALLOWED_ORIGINS="${MINIO_ALLOWED_ORIGINS:-http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173}"

# 配置 MinIO 别名
echo "📝 配置 MinIO 连接..."
mc alias set myminio $MINIO_ENDPOINT $MINIO_USER $MINIO_PASS

if [ $? -ne 0 ]; then
    echo "❌ 连接 MinIO 失败，请确保 MinIO 服务已启动"
    echo ""
    echo "启动 MinIO:"
    echo "  minio server ~/minio-data"
    echo ""
    exit 1
fi

echo "✅ MinIO 连接成功"
echo ""

# 创建 bucket
echo "📦 创建 bucket: $BUCKET_NAME..."
mc mb myminio/$BUCKET_NAME 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Bucket 创建成功"
else
    echo "ℹ️  Bucket 已存在，跳过创建"
fi
echo ""

# 设置公开访问（下载策略）
echo "🔓 设置 bucket 为公开访问..."
mc anonymous set download myminio/$BUCKET_NAME

if [ $? -eq 0 ]; then
    echo "✅ 下载访问策略设置成功"
else
    echo "⚠️  访问策略设置失败，尝试使用 public 策略..."
    mc anonymous set public myminio/$BUCKET_NAME
fi
echo ""

# 设置全局跨域
echo "🌐 设置 MinIO 全局跨域..."
mc admin config set myminio/ api cors_allow_origin="$ALLOWED_ORIGINS"

if [ $? -eq 0 ]; then
    echo "✅ 全局跨域设置成功"
    echo "♻️  重启 MinIO 服务使配置生效..."
    mc --json admin service restart --wait myminio/ >/dev/null
    echo ""
    echo "当前 API 配置:"
    mc admin config get myminio api
else
    echo "❌ 全局跨域设置失败"
    exit 1
fi
echo ""

# 创建文件夹结构
echo "📁 创建文件夹结构..."
folders=("avatars" "certificates" "dishes" "temp")
for folder in "${folders[@]}"; do
    echo "" | mc pipe myminio/$BUCKET_NAME/$folder/.keep 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "  ✅ $folder/"
    else
        echo "  ⚠️  $folder/ (可能已存在)"
    fi
done
echo ""

# 显示配置信息
echo "================================"
echo "✅ MinIO 配置完成！"
echo "================================"
echo ""
echo "配置信息:"
echo "  API 地址: $MINIO_ENDPOINT"
echo "  用户名:   $MINIO_USER"
echo "  密码:     $MINIO_PASS"
echo "  Bucket:   $BUCKET_NAME"
echo ""
echo "文件夹结构:"
echo "  $BUCKET_NAME/"
echo "  ├── avatars/      (用户头像)"
echo "  ├── certificates/ (厨师证件)"
echo "  ├── dishes/       (菜品图片)"
echo "  └── temp/         (临时文件)"
echo ""
echo "测试命令:"
echo "  # 上传测试文件"
echo "  echo 'test' | mc pipe myminio/$BUCKET_NAME/test.txt"
echo ""
echo "  # 查看文件列表"
echo "  mc ls myminio/$BUCKET_NAME"
echo ""
echo "  # 查看全局跨域配置"
echo "  mc admin config get myminio api"
echo ""
echo "  # 测试访问（应该返回 200）"
echo "  curl -I $MINIO_ENDPOINT/$BUCKET_NAME/test.txt"
echo ""
echo "  # 测试浏览器预检（应该返回 204，并带 Access-Control-Allow-Origin）"
echo "  curl -i -X OPTIONS $MINIO_ENDPOINT/$BUCKET_NAME/test.txt -H 'Origin: http://localhost:5173' -H 'Access-Control-Request-Method: PUT' -H 'Access-Control-Request-Headers: content-type'"
echo ""
echo "================================"
echo ""
echo "💡 提示："
echo "  - 如果你的前端端口不是 5173/4173，可以这样执行："
echo "    MINIO_ALLOWED_ORIGINS='http://localhost:3000,http://127.0.0.1:3000' ./setup-minio.sh"
echo "  - 这台 MinIO 不支持 bucket 级 mc cors set，因此改为全局 api.cors_allow_origin 配置"
echo "  - 修改允许来源后，重新执行本脚本即可覆盖跨域配置"
echo ""
