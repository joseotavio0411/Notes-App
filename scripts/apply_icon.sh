#!/bin/bash
set -e

# Find image file from arguments or standard locations
SRC=""
if [ -n "$1" ] && [ -f "$1" ]; then
    SRC="$1"
elif [ -f "image.png" ]; then
    SRC="image.png"
elif [ -f "app/image.png" ]; then
    SRC="app/image.png"
elif [ -f "app/src/main/res/drawable/image.png" ]; then
    SRC="app/src/main/res/drawable/image.png"
elif [ -f "app/src/main/res/drawable/app_icon.png" ]; then
    SRC="app/src/main/res/drawable/app_icon.png"
elif [ -f "app_icon.png" ]; then
    SRC="app_icon.png"
fi

if [ -z "$SRC" ]; then
    echo "Nenhuma imagem encontrada para o ícone (ex: image.png)."
    exit 1
fi

echo "Processando ícone a partir de: $SRC"

# Target drawable
DEST_DRAWABLE="app/src/main/res/drawable/app_icon_custom.png"
convert "$SRC" "PNG32:$DEST_DRAWABLE"

# Update foreground layer
cat << 'EOF' > app/src/main/res/drawable/ic_launcher_foreground.xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:width="72dp"
        android:height="72dp"
        android:drawable="@drawable/app_icon_custom"
        android:gravity="center" />
</layer-list>
EOF

# Update densities
BASE="app/src/main/res"
for spec in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  dir="$BASE/mipmap-${spec%:*}"
  size="${spec#*:}"
  mkdir -p "$dir"
  rm -f "$dir/ic_launcher.webp" "$dir/ic_launcher_round.webp"
  convert "$SRC" -resize "${size}x${size}!" "PNG32:$dir/ic_launcher.png"
  radius=$((size / 2))
  convert "$SRC" -resize "${size}x${size}!" \
    \( -size "${size}x${size}" xc:none -fill white \
       -draw "circle $radius,$radius $radius,0" \) \
    -alpha set -compose DstIn -composite "PNG32:$dir/ic_launcher_round.png"
done

echo "Ícone aplicado com sucesso em todas as densidades!"
