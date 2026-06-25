#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import cairosvg, os

# SVG логотипа ПОДАЧА: золотая буква "П" на тёмном фоне
SVG = (
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500">'
    '<rect width="500" height="500" fill="#1a1a2e"/>'
    '<circle cx="250" cy="250" r="220" fill="none" stroke="#b8942e" stroke-width="10"/>'
    '<path d="M180 150 L320 150 L320 230 L250 230 L250 350 L180 350 Z" fill="#b8942e"/>'
    '</svg>'
)

BASE = os.path.join(os.path.dirname(__file__), 'app', 'src', 'main')

SIZES = [
    ('mipmap-mdpi', 48),
    ('mipmap-hdpi', 72),
    ('mipmap-xhdpi', 96),
    ('mipmap-xxhdpi', 144),
    ('mipmap-xxxhdpi', 192),
]

for folder, size in SIZES:
    d = os.path.join(BASE, folder)
    os.makedirs(d, exist_ok=True)
    
    png_data = cairosvg.svg2png(bytestring=SVG.encode('utf-8'), output_width=size, output_height=size)
    
    with open(os.path.join(d, 'ic_launcher.png'), 'wb') as f:
        f.write(png_data)
    with open(os.path.join(d, 'ic_launcher_round.png'), 'wb') as f:
        f.write(png_data)
    
    print(f'OK {folder} ({size}x{size})')

print('\nDone! All icons created.')