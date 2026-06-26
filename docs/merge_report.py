#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""合并 6 个 report-parts 为统一的 04-期末综合设计报告.md，代码块感知地规范标题层级。"""
import os, re

BASE = os.path.dirname(os.path.abspath(__file__))
PARTS_DIR = os.path.join(BASE, "report-parts")
OUT = os.path.join(BASE, "04-期末综合设计报告.md")

# 每个 part 的处理规则：
#   keep        : 原样
#   strip1      : 删除第1行（"第X部分"壳标题），其余原样
#   demote1     : 所有正文标题（非代码块内）整体降一级（# -> ##）
RULES = {
    "part1.md": "keep",
    "part2.md": "strip1",
    "part3.md": "demote1",
    "part4.md": "demote1",
    "part5.md": "strip1",
    "part6.md": "demote1",
}

HEADING_RE = re.compile(r'^(#{1,6})(\s+\S)')

def process(text, rule):
    lines = text.split("\n")
    if rule == "strip1":
        # 删除开头的壳标题行（及其后可能的空行）
        # 找到第一行 # 开头的，删掉它
        out = []
        removed = False
        for i, ln in enumerate(lines):
            if not removed and ln.startswith("# "):
                removed = True
                continue
            out.append(ln)
        lines = out
        rule = "keep"

    if rule == "keep":
        return "\n".join(lines)

    # demote1：代码块感知地给标题加一个 #
    in_fence = False
    fence_marker = None
    out = []
    for ln in lines:
        stripped = ln.lstrip()
        # 检测代码围栏 ``` 或 ~~~
        m = re.match(r'^(```+|~~~+)', stripped)
        if m:
            marker = m.group(1)[:3]
            if not in_fence:
                in_fence = True
                fence_marker = marker
            elif stripped.startswith(fence_marker):
                in_fence = False
                fence_marker = None
            out.append(ln)
            continue
        if not in_fence and HEADING_RE.match(ln):
            out.append("#" + ln)  # 降一级
        else:
            out.append(ln)
    return "\n".join(out)

merged = []
for part, rule in RULES.items():
    p = os.path.join(PARTS_DIR, part)
    with open(p, "r", encoding="utf-8") as f:
        txt = f.read()
    processed = process(txt, rule)
    merged.append(processed.rstrip())
    merged.append("\n")  # part 之间留空

final = "\n".join(merged).rstrip() + "\n"
with open(OUT, "w", encoding="utf-8") as f:
    f.write(final)

# 统计
nlines = final.count("\n") + 1
h1 = len(re.findall(r'(?m)^# \S', final))
h2 = len(re.findall(r'(?m)^## \S', final))
h3 = len(re.findall(r'(?m)^### \S', final))
print(f"已生成 {OUT}")
print(f"总行数: {nlines}")
print(f"H1(应=1): {h1}  H2(章节): {h2}  H3(小节): {h3}")
