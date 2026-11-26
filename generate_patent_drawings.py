#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate Patent Technical Drawings for Iranian Intellectual Property Office
Conforming strictly to 'راهنماي_تهيه_فايل_نقشه.pdf':
- Pure Black & White / Grayscale line drawings
- No solid color fills, no real photos
- Numbered reference elements (100, 101, 102...)
- Vector/High-resolution clean line art
"""

import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches

os.makedirs('patent_drawings', exist_ok=True)

# Set global styles: Black lines, white backgrounds, clean typography
plt.rcParams['font.sans-serif'] = 'DejaVu Sans'
plt.rcParams['axes.edgecolor'] = 'black'
plt.rcParams['axes.linewidth'] = 1.2

# --- FIGURE 1: SYSTEM ARCHITECTURE BLOCK DIAGRAM ---
def draw_figure_1():
    fig, ax = plt.subplots(figsize=(10, 8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    # Title / Label
    ax.text(50, 96, "FIG. 1 (شکل ۱)", fontsize=14, fontweight='bold', ha='center', va='center')
    ax.text(50, 92, "System Architecture Block Diagram (نمودار بلوکی معماری سامانه)", fontsize=10, ha='center', va='center')

    # Main System Boundary (100)
    rect_sys = patches.Rectangle((4, 4), 92, 84, fill=False, edgecolor='black', linewidth=1.8, linestyle='--')
    ax.add_patch(rect_sys)
    ax.text(8, 85, "100: Distributed Deduction System (سامانه توزیع شده)", fontsize=9, fontweight='bold')

    # Client Terminals (101)
    rect_c1 = patches.Rectangle((8, 52), 26, 26, fill=False, edgecolor='black', linewidth=1.4)
    ax.add_patch(rect_c1)
    ax.text(21, 74, "101a: Client Terminal 1\n(پایانه کاربر ۱)", fontsize=8, ha='center', va='center')
    ax.text(21, 64, "102: Local UI Engine\n(رابط کاربری و تخته معما)", fontsize=7, ha='center', va='center')
    ax.text(21, 56, "103: Local Storage / State\n(پایگاه داده محلی)", fontsize=7, ha='center', va='center')

    rect_c2 = patches.Rectangle((8, 12), 26, 26, fill=False, edgecolor='black', linewidth=1.4)
    ax.add_patch(rect_c2)
    ax.text(21, 34, "101n: Client Terminal N\n(پایانه کاربر N)", fontsize=8, ha='center', va='center')
    ax.text(21, 24, "102: Local UI Engine\n(رابط کاربری و تخته معما)", fontsize=7, ha='center', va='center')
    ax.text(21, 16, "103: Local Storage / State\n(پایگاه داده محلی)", fontsize=7, ha='center', va='center')

    ax.text(21, 44, ". . .", fontsize=14, fontweight='bold', ha='center', va='center')

    # Local Network Layer (104)
    rect_net = patches.Rectangle((42, 22), 16, 50, fill=False, edgecolor='black', linewidth=1.4, linestyle=':')
    ax.add_patch(rect_net)
    ax.text(50, 48, "104:\nLocal Offline\nNetwork Bus\n(mDNS / TCP Socket)\n(شبکه محلی آفلاین)", fontsize=8, ha='center', va='center')

    # Host Server Engine (105)
    rect_host = patches.Rectangle((66, 10), 26, 72, fill=False, edgecolor='black', linewidth=1.6)
    ax.add_patch(rect_host)
    ax.text(79, 78, "105: Host Authority Node\n(گره سرور میزبان)", fontsize=9, fontweight='bold', ha='center', va='center')

    # Sub-modules inside Host
    rect_sub1 = patches.Rectangle((68, 62), 22, 11, fill=False, edgecolor='black', linewidth=1.0)
    ax.add_patch(rect_sub1)
    ax.text(79, 67.5, "106: Invariant & Package\nSolver (موتور افراز و توزیع)", fontsize=7, ha='center', va='center')

    rect_sub2 = patches.Rectangle((68, 48), 22, 11, fill=False, edgecolor='black', linewidth=1.0)
    ax.add_patch(rect_sub2)
    ax.text(79, 53.5, "107: Asymmetric Joker\nEngine (موتور توازن جوکر)", fontsize=7, ha='center', va='center')

    rect_sub3 = patches.Rectangle((68, 34), 22, 11, fill=False, edgecolor='black', linewidth=1.0)
    ax.add_patch(rect_sub3)
    ax.text(79, 39.5, "108: Mystery Evaluation\nEngine (موتور ارزیابی حدس)", fontsize=7, ha='center', va='center')

    rect_sub4 = patches.Rectangle((68, 20), 22, 11, fill=False, edgecolor='black', linewidth=1.0)
    ax.add_patch(rect_sub4)
    ax.text(79, 25.5, "109: Will & Strikeout\nEngine (موتور وصیت و حذف)", fontsize=7, ha='center', va='center')

    # Communication Arrows
    ax.annotate('', xy=(42, 65), xytext=(34, 65), arrowprops=dict(arrowstyle="<->", color="black", lw=1.2))
    ax.annotate('', xy=(42, 25), xytext=(34, 25), arrowprops=dict(arrowstyle="<->", color="black", lw=1.2))
    ax.annotate('', xy=(66, 48), xytext=(58, 48), arrowprops=dict(arrowstyle="<->", color="black", lw=1.2))

    plt.tight_layout()
    plt.savefig('patent_drawings/fig1_system_architecture.png', dpi=300)
    plt.close()

# --- FIGURE 2: METHOD WORKFLOW FLOWCHART ---
def draw_figure_2():
    fig, ax = plt.subplots(figsize=(9, 10), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    ax.text(50, 97, "FIG. 2 (شکل ۲)", fontsize=14, fontweight='bold', ha='center', va='center')
    ax.text(50, 94, "Overall Method Operational Flowchart (فلوچارت کلی روش اجرای اختراع)", fontsize=10, ha='center', va='center')

    steps = [
        ("201: Start & Discovery of Host Node via mDNS (آغاز و کشف گره میزبان)", 86),
        ("202: Join Lobby & Player Registration (تشکیل لابی و ثبت کارآگاهان)", 76),
        ("203: Secret Joker Role Selection & Ceiling Calc (انتخاب محرمانه جوکر)", 66),
        ("204: Question Creation & Handwritten Inquiries (طرح سوالات دست‌نویس)", 56),
        ("205: Package Assembly & Derangement Distribution (افراز و توزیع بسته‌ها)", 46),
        ("206: Investigation & Comms Wire Interrogation (فاز بازجویی و تقاطع سرنخ)", 36),
        ("207: Strikeout Evaluation / Will Protocol Trigger (ارزیابی خطاها و وصیت‌نامه)", 26),
        ("208: Victory Condition Evaluation / Case Termination (پایان پرونده و تعیین برنده)", 16),
        ("209: Strictly Conditional Archive to Gallery (بایگانی مشروط در گالری)", 6)
    ]

    for text, y in steps:
        rect = patches.FancyBboxPatch((15, y - 3.5), 70, 7, boxstyle="round,pad=0.5", fill=False, edgecolor='black', linewidth=1.2)
        ax.add_patch(rect)
        ax.text(50, y, text, fontsize=8, ha='center', va='center', fontweight='bold')

    for i in range(len(steps) - 1):
        y_from = steps[i][1] - 4
        y_to = steps[i+1][1] + 4
        ax.annotate('', xy=(50, y_to), xytext=(50, y_from), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))

    plt.tight_layout()
    plt.savefig('patent_drawings/fig2_method_flowchart.png', dpi=300)
    plt.close()

# --- FIGURE 3: SINGLE PACKAGE INFORMATION PARTITION ---
def draw_figure_3():
    fig, ax = plt.subplots(figsize=(10, 8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    ax.text(50, 96, "FIG. 3 (شکل ۳)", fontsize=14, fontweight='bold', ha='center', va='center')
    ax.text(50, 92, "Single Package Information Partition Model (مدل افراز تک‌بسته‌ای اطلاعات)", fontsize=10, ha='center', va='center')

    # Author (301)
    rect_auth = patches.Rectangle((8, 62), 26, 20, fill=False, edgecolor='black', linewidth=1.3)
    ax.add_patch(rect_auth)
    ax.text(21, 74, "301: Author Detective\n(کارآگاه طراح سوال)", fontsize=8, fontweight='bold', ha='center', va='center')
    ax.text(21, 66, "Creates 3 Inquiries\n(طرح ۳ سوال)", fontsize=7, ha='center', va='center')

    # Answerer (302)
    rect_ans = patches.Rectangle((66, 62), 26, 20, fill=False, edgecolor='black', linewidth=1.3)
    ax.add_patch(rect_ans)
    ax.text(79, 74, "302: Answerer Detective\n(کارآگاه پاسخ‌دهنده)", fontsize=8, fontweight='bold', ha='center', va='center')
    ax.text(79, 66, "Answers 3 Inquiries\n(ثبت ۳ پاسخ)", fontsize=7, ha='center', va='center')

    # Sealed Package (303)
    rect_pkg = patches.Rectangle((32, 44), 36, 22, fill=False, edgecolor='black', linewidth=1.6, linestyle='--')
    ax.add_patch(rect_pkg)
    ax.text(50, 58, "303: Sealed Evidence Package\n(بسته اطلاعاتی مهروموم)", fontsize=8, fontweight='bold', ha='center', va='center')
    ax.text(50, 49, "Invariant: Author != Answerer\n(طراح != پاسخ‌دهنده)", fontsize=7, ha='center', va='center')

    # Single Recipient (304)
    rect_rec = patches.Rectangle((28, 12), 44, 20, fill=False, edgecolor='black', linewidth=1.4)
    ax.add_patch(rect_rec)
    ax.text(50, 24, "304: Recipient Detective (کارآگاه دریافت‌کننده بسته مدارک)", fontsize=8.5, fontweight='bold', ha='center', va='center')
    ax.text(50, 16, "Invariants: Recipient != Author AND Recipient != Answerer\nExactly 1 Package Received per Detective (تحویل دقیقاً ۱ بسته به هر کارآگاه)", fontsize=7.5, ha='center', va='center')

    # Connecting arrows
    ax.annotate('', xy=(42, 66), xytext=(34, 72), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
    ax.annotate('', xy=(58, 66), xytext=(66, 72), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
    ax.annotate('', xy=(50, 32), xytext=(50, 44), arrowprops=dict(arrowstyle="->", color="black", lw=1.4))

    plt.tight_layout()
    plt.savefig('patent_drawings/fig3_information_partition.png', dpi=300)
    plt.close()

# --- FIGURE 4: ASYMMETRIC JOKER STATE MACHINE ---
def draw_figure_4():
    fig, ax = plt.subplots(figsize=(9, 8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    ax.text(50, 96, "FIG. 4 (شکل ۴)", fontsize=14, fontweight='bold', ha='center', va='center')
    ax.text(50, 92, "Asymmetric Joker State Machine & Formula (ماشین وضعیت نقش جوکر)", fontsize=10, ha='center', va='center')

    # Math formula box (401)
    rect_f = patches.Rectangle((18, 74), 64, 13, fill=False, edgecolor='black', linewidth=1.4)
    ax.add_patch(rect_f)
    ax.text(50, 82, "401: Dynamic Ceiling Target Formula: T = ceil(2 * N / 3)", fontsize=9, fontweight='bold', ha='center', va='center')
    ax.text(50, 77, "For N=4: T=3 | For N=6: T=4 | For N=8: T=6", fontsize=8, ha='center', va='center')

    # State Active (402)
    circle_active = patches.Circle((50, 52), 12, fill=False, edgecolor='black', linewidth=1.5)
    ax.add_patch(circle_active)
    ax.text(50, 54, "402: ACTIVE", fontsize=9, fontweight='bold', ha='center', va='center')
    ax.text(50, 48, "(فعال و در جریان)", fontsize=7.5, ha='center', va='center')

    # State WON (403)
    circle_won = patches.Circle((22, 20), 12, fill=False, edgecolor='black', linewidth=1.8)
    ax.add_patch(circle_won)
    circle_won_in = patches.Circle((22, 20), 10.5, fill=False, edgecolor='black', linewidth=1.0)
    ax.add_patch(circle_won_in)
    ax.text(22, 22, "403: JOKER WON", fontsize=8.5, fontweight='bold', ha='center', va='center')
    ax.text(22, 16, "(پیروزی جوکر)", fontsize=7.5, ha='center', va='center')

    # State LOST (404)
    circle_lost = patches.Circle((78, 20), 12, fill=False, edgecolor='black', linewidth=1.8)
    ax.add_patch(circle_lost)
    circle_lost_in = patches.Circle((78, 20), 10.5, fill=False, edgecolor='black', linewidth=1.0)
    ax.add_patch(circle_lost_in)
    ax.text(78, 22, "404: JOKER LOST", fontsize=8.5, fontweight='bold', ha='center', va='center')
    ax.text(78, 16, "(شکست جوکر)", fontsize=7.5, ha='center', va='center')

    # Transitions
    ax.annotate('Eliminations >= T\n(تکمیل سقف حذفیات)', xy=(28, 30), xytext=(40, 46),
                arrowprops=dict(arrowstyle="->", color="black", lw=1.2), fontsize=7.5, ha='center')

    ax.annotate('Detective Solves 4 Mysteries\n(کشف کامل معماها)', xy=(72, 30), xytext=(60, 46),
                arrowprops=dict(arrowstyle="->", color="black", lw=1.2), fontsize=7.5, ha='center')

    plt.tight_layout()
    plt.savefig('patent_drawings/fig4_joker_state_machine.png', dpi=300)
    plt.close()

# --- FIGURE 5: STRIKEOUT & WILL PROTOCOL ---
def draw_figure_5():
    fig, ax = plt.subplots(figsize=(9, 9), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    ax.text(50, 96, "FIG. 5 (شکل ۵)", fontsize=14, fontweight='bold', ha='center', va='center')
    ax.text(50, 92, "3-Strike Elimination & Testament Protocol (فلوچارت ۳ خطا و وصیت‌نامه)", fontsize=10, ha='center', va='center')

    # 501: Detective makes Guess
    p1 = patches.Rectangle((25, 78), 50, 9, fill=False, edgecolor='black', linewidth=1.2)
    ax.add_patch(p1)
    ax.text(50, 82.5, "501: Detective Submits Guess on Mystery Board\n(ارسال فرضیه کارآگاهی توسط بازیکن)", fontsize=8, ha='center', va='center')

    # 502: Mystery Validator Decision
    diamond = patches.Polygon([[50, 72], [70, 62], [50, 52], [30, 62]], fill=False, edgecolor='black', linewidth=1.2)
    ax.add_patch(diamond)
    ax.text(50, 62, "502: Correct?\n(صحیح است؟)", fontsize=8, fontweight='bold', ha='center', va='center')

    # If correct
    ax.annotate('YES (بله)', xy=(70, 62), xytext=(85, 62), arrowprops=dict(arrowstyle="->", color="black", lw=1.2), fontsize=7.5)
    p_win = patches.Rectangle((75, 42), 22, 14, fill=False, edgecolor='black', linewidth=1.2)
    ax.add_patch(p_win)
    ax.text(86, 49, "503: Mark Solved\n(ثبت سرنخ حل‌شده)\nIf 4 Solved -> Victory", fontsize=7, ha='center', va='center')
    ax.annotate('', xy=(86, 56), xytext=(86, 62), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))

    # If incorrect
    ax.annotate('NO (خیر)', xy=(50, 52), xytext=(50, 44), arrowprops=dict(arrowstyle="->", color="black", lw=1.2), fontsize=7.5, ha='center')
    p_strike = patches.Rectangle((30, 36), 40, 8, fill=False, edgecolor='black', linewidth=1.2)
    ax.add_patch(p_strike)
    ax.text(50, 40, "504: Increment Mistakes Counter (M = M + 1)\n(افزایش شمارنده اشتباهات)", fontsize=7.5, ha='center', va='center')

    # Check M >= 3
    ax.annotate('', xy=(50, 28), xytext=(50, 36), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
    p_elim = patches.Rectangle((20, 18), 60, 10, fill=False, edgecolor='black', linewidth=1.4)
    ax.add_patch(p_elim)
    ax.text(50, 23, "505: If M >= 3 -> Player Eliminated (حذف از بازی)\nState Transition -> WillStatus.PENDING", fontsize=8, fontweight='bold', ha='center', va='center')

    # Will submission
    ax.annotate('', xy=(50, 10), xytext=(50, 18), arrowprops=dict(arrowstyle="->", color="black", lw=1.2))
    p_will = patches.Rectangle((15, 2), 70, 8, fill=False, edgecolor='black', linewidth=1.2)
    ax.add_patch(p_will)
    ax.text(50, 6, "506: Submit Sealed Testament (Accusation / Revelation) -> Public Comms Wire Broadcast", fontsize=7.5, ha='center', va='center')

    plt.tight_layout()
    plt.savefig('patent_drawings/fig5_strikeout_and_will.png', dpi=300)
    plt.close()

# --- FIGURE 6: TIMEOUT RESOLUTION PRIORITY MATRIX ---
def draw_figure_6():
    fig, ax = plt.subplots(figsize=(9, 8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    ax.text(50, 96, "FIG. 6 (شکل ۶)", fontsize=14, fontweight='bold', ha='center', va='center')
    ax.text(50, 92, "Deterministic Timeout Priority Tree (درخت اولویت حل در انقضای زمان)", fontsize=10, ha='center', va='center')

    boxes = [
        ("601: Game Expiry Timer Reached (T = 8 Hours) (اتمام سقف زمان ۸ ساعت)", 82, 70),
        ("602: Priority 1: Maximum Solved Clues (بیشترین سرنخ حل‌شده: 3 > 2 > 1)", 66, 68),
        ("603: Priority 2: Tie-Breaker: Fewest Mistakes (کمترین اشتباه در تساوی: 0 < 1 < 2)", 50, 68),
        ("604: Priority 3: Tie-Breaker: Fastest Submission Timestamp (سرعت ثبت سرنخ)", 34, 68),
        ("605: Deterministic Victor Resolution & State Lock (اعلام قطعی برنده پرونده)", 18, 68)
    ]

    for text, y, w in boxes:
        rect = patches.Rectangle((50 - w/2, y - 4), w, 8, fill=False, edgecolor='black', linewidth=1.2)
        ax.add_patch(rect)
        ax.text(50, y, text, fontsize=8, ha='center', va='center', fontweight='bold')

    for i in range(len(boxes) - 1):
        ax.annotate('', xy=(50, boxes[i+1][1] + 4), xytext=(50, boxes[i][1] - 4),
                    arrowprops=dict(arrowstyle="->", color="black", lw=1.2))

    plt.tight_layout()
    plt.savefig('patent_drawings/fig6_timeout_priority.png', dpi=300)
    plt.close()

if __name__ == '__main__':
    draw_figure_1()
    draw_figure_2()
    draw_figure_3()
    draw_figure_4()
    draw_figure_5()
    draw_figure_6()
    print("All 6 figures generated successfully in patent_drawings/")
