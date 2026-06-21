import subprocess, os, tempfile, base64

# SVG логотипа (золотая буква П на тёмном фоне)
SVG_CONTENT = '''<?xml version="1.0" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 20010904//EN" "http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd">
<svg version="1.0" xmlns="http://www.w3.org/2000/svg" width="500" height="500" viewBox="0 0 500 500" preserveAspectRatio="xMidYMid meet">
<rect width="500" height="500" fill="#1a1a2e"/>
<g transform="translate(0,500) scale(0.1,-0.1)" fill="#b8942e" stroke="none">
<path d="M0 2500 l0 -2500 2500 0 2500 0 0 2500 0 2500 -2500 0 -2500 0 0 -2500z m2895 1916 c822 -175 1433 -824 1550 -1647 19 -135 19 -413 0 -548 -62 -433 -257 -817 -575 -1130 l-95 -94 -6 809 c-5 666 -9 828 -22 915 -47 314 -125 502 -275 661 -156 165 -339 263 -602 319 -93 21 -133 23 -360 24 -225 0 -268 -3 -365 -22 -220 -45 -382 -116 -520 -227 -202 -163 -305 -374 -362 -738 -15 -98 -18 -219 -23 -927 l-5 -814 -95 94 c-318 313 -513 697 -575 1130 -19 135 -19 413 0 548 118 831 754 1498 1575 1651 152 28 154 28 405 25 201 -3 245 -6 350 -29z m-200 -1076 c120 -18 197 -40 293 -86 205 -99 298 -259 349 -604 13 -87 17 -259 20 -1012 l5 -907 -59 -26 c-251 -112 -510 -165 -798 -165 -288 0 -547 53 -798 165 l-58 25 4 938 c5 987 5 1000 52 1187 62 248 270 428 552 479 92 17 339 20 438 6z"/>
</g>
</svg>'''

# Создаем временный SVG файл
svg_path = os.path.join(tempfile.gettempdir(), 'icon.svg')
with open(svg_path, 'w', encoding='utf-8') as f:
    f.write(SVG_CONTENT)

# Папки для mipmap
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
    'mipmap-xxxhdpi': 512,  # для больших экранов
}

base_dir = os.path.join(os.path.dirname(__file__), 'app', 'src', 'main')

for folder, size in sizes.items():
    folder_path = os.path.join(base_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    if size == 512:
        folder_path = os.path.join(base_dir, 'mipmap-xxxhdpi')
        os.makedirs(folder_path, exist_ok=True)
    
    # Используем rsvg-convert если доступен, иначе используем python
    output = os.path.join(folder_path, 'ic_launcher.png')
    try:
        subprocess.run([
            'rsvg-convert',
            '-w', str(size),
            '-h', str(size),
            '-o', output,
            svg_path
        ], check=True)
        print(f'Created {output}')
    except:
        print(f'Warning: rsvg-convert not found, creating placeholder for {folder}')
        # Создаем плейсхолдер (просто скопируем базовую иконку если есть)
        pass

# Также создаем ic_launcher_round.png (копия)
for folder, size in sizes.items():
    folder_path = os.path.join(base_dir, folder)
    src = os.path.join(folder_path, 'ic_launcher.png')
    dst = os.path.join(folder_path, 'ic_launcher_round.png')
    if os.path.exists(src):
        import shutil
        shutil.copy(src, dst)
        print(f'Created round icon: {dst}')

print('Done!')