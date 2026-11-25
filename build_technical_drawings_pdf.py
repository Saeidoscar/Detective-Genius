#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Build official Technical Drawings PDF for Iranian Intellectual Property Office
Page size: A4, 1 figure per page, black & white, page numbers, under 3.0 MB
"""

from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from PIL import Image
import os

images = [
    ("patent_drawings/fig1_system_architecture.png", "شکل ۱: نمودار بلوکی ساختار سامانه توزیع‌شده (System Architecture)"),
    ("patent_drawings/fig2_method_flowchart.png", "شکل ۲: فلوچارت کلی مراحل اجرای روش (Method Flowchart)"),
    ("patent_drawings/fig3_information_partition.png", "شکل ۳: مدل افراز اطلاعات و توزیع بسته‌ها (Information Partition)"),
    ("patent_drawings/fig4_joker_state_machine.png", "شکل ۴: ماشین وضعیت نقش نامتقارن جوکر (Joker State Machine)"),
    ("patent_drawings/fig5_strikeout_and_will.png", "شکل ۵: فلوچارت مکانیزم ۳ خطا و پروتکل وصیت‌نامه (Strikeout & Will)"),
    ("patent_drawings/fig6_timeout_priority.png", "شکل ۶: درخت اولویت حل در انقضای زمان (Timeout Priority Matrix)")
]

pdf_path = "نقشه_فنی_اختراع.pdf"
c = canvas.Canvas(pdf_path, pagesize=A4)
width, height = A4 # 595.27, 841.89 points

total_pages = len(images)

for idx, (img_path, caption) in enumerate(images):
    page_num = idx + 1
    
    # Header: Reference / Page count
    c.setFont("Helvetica-Bold", 10)
    c.drawString(50, height - 40, f"FIG. {page_num}")
    c.drawRightString(width - 50, height - 40, f"Page {page_num} of {total_pages}")
    
    # Draw line below header
    c.setLineWidth(0.5)
    c.line(50, height - 48, width - 50, height - 48)

    # Insert Image centered
    # Max box: width: 495 pt, height: 680 pt
    max_w = width - 100
    max_h = height - 140

    with Image.open(img_path) as im:
        img_w, img_h = im.size
        ratio = min(max_w / img_w, max_h / img_h)
        dest_w = img_w * ratio
        dest_h = img_h * ratio
        dest_x = (width - dest_w) / 2
        dest_y = (height - dest_h) / 2 - 10

        c.drawImage(img_path, dest_x, dest_y, width=dest_w, height=dest_h)

    # Footer line & Caption
    c.line(50, 50, width - 50, 50)
    c.setFont("Helvetica", 9)
    c.drawRightString(width - 50, 35, f"Patent Application - Technical Drawing Sheet {page_num}/{total_pages}")

    c.showPage()

c.save()

print(f"Generated {pdf_path} (Size: {os.path.getsize(pdf_path) / 1024:.1f} KB)")
