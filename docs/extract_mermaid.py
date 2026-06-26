#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""提取 04-期末综合设计报告.md 中的 mermaid 代码块为独立 .mmd 文件，
   并生成一份把 mermaid 块替换为图片引用的中间 md（供 pandoc 转 docx）。"""
import os, re

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "04-期末综合设计报告.md")
IMG_MD = os.path.join(BASE, "04-期末综合设计报告-img.md")
IMG_DIR = os.path.join(BASE, "images")
os.makedirs(IMG_DIR, exist_ok=True)

with open(SRC, "r", encoding="utf-8") as f:
    lines = f.read().split("\n")

out = []
i = 0
n = 0
in_fence = False          # 普通代码围栏
diagrams = []
while i < len(lines):
    ln = lines[i]
    stripped = ln.strip()
    # 检测 mermaid 围栏开始
    if not in_fence and re.match(r'^```mermaid\s*$', stripped):
        # 收集到对应的 ```
        j = i + 1
        body = []
        while j < len(lines) and lines[j].strip() != "```":
            body.append(lines[j])
            j += 1
        n += 1
        name = f"diagram-{n:02d}"
        with open(os.path.join(IMG_DIR, name + ".mmd"), "w", encoding="utf-8") as mf:
            mf.write("\n".join(body).strip() + "\n")
        diagrams.append((name, "\n".join(body[:1])))
        # 在中间 md 中用图片引用替换（居中、带图号）
        out.append("")
        out.append(f"![图 {n}](images/{name}.png)")
        out.append("")
        i = j + 1  # 跳过结尾 ```
        continue
    # 跟踪普通围栏，避免把代码块里的内容误判
    if re.match(r'^```', stripped):
        in_fence = not in_fence
    out.append(ln)
    i += 1

with open(IMG_MD, "w", encoding="utf-8") as f:
    f.write("\n".join(out))

print(f"提取 mermaid 图 {n} 个 -> {IMG_DIR}")
print(f"生成中间 md -> {IMG_MD}")
for name, head in diagrams:
    print(f"  {name}: {head.strip()}")
