#!/usr/bin/env bash
# theme.sh's twin for the devices Android Studio uses: pushes a theme's config.json to the launcher and restarts it,
# with the adb Android Studio runs. By default the one emulator that is up (adb -e); ANDROID_SERIAL picks any other
# device adb knows, such as the TV it installed to. Wallpapers are not pushed: config.json only.
#
# Usage: tools/theme-emu.sh my-theme                                      # the emulator
#        ANDROID_SERIAL=192.168.0.247:5555 tools/theme-emu.sh my-theme    # the TV, as Android Studio lists it
#        tools/theme-emu.sh --reset                                       # back to the built-in config.xml
set -euo pipefail
PKG=com.gathin.tvhome
URI=content://$PKG.theme
DIR=/sdcard/Android/data/$PKG/files
TMP=/data/local/tmp/cyt-theme
if [ -n "${ANDROID_SERIAL:-}" ]; then
  ADB=(adb -s "$ANDROID_SERIAL")
else
  ADB=(adb -e)
fi

"${ADB[@]}" get-state >/dev/null 2>&1 \
  || { echo "device not reachable: start the emulator, or set ANDROID_SERIAL to a device from 'adb devices'" >&2; exit 1; }

# The launcher's theme provider first, as theme.sh does; a plain copy where it is not there.
put() {
  "${ADB[@]}" shell "rm -f $DIR/$1 2>/dev/null; { o=\$(content write --uri $URI/$1 < $TMP-$1 2>&1) && case \"\$o\" in *'Unsupported operation'*) false ;; esac; } || cp $TMP-$1 $DIR/$1; rm -f $TMP-$1"
}
del() {
  "${ADB[@]}" shell "rm -f $DIR/$1 2>/dev/null; content delete --uri $URI/$1 2>/dev/null; true"
}
# A new revision tells the launcher to drop the choices made in its on-TV settings, as after a theme.sh push.
revision() {
  "${ADB[@]}" shell "printf '%s' '$(uuidgen | tr -d '-')' > $TMP-theme_revision"
  put theme_revision
}
restart() {
  "${ADB[@]}" shell "am force-stop $PKG; am start -n $PKG/.MainActivity" >/dev/null
}

"${ADB[@]}" shell "mkdir -p $DIR" >/dev/null 2>&1 || true

if [ "${1:-}" = --reset ]; then
  del config.json
  revision
  restart
  echo reset
  exit 0
fi

THEME="${1:?usage: tools/theme-emu.sh <theme folder> | --reset}"
[ -f "$THEME/config.json" ] || { echo "no config.json in $THEME" >&2; exit 1; }
python3 -c 'import json,sys; json.load(open(sys.argv[1], encoding="utf-8-sig"))' "$THEME/config.json" 2>/dev/null \
  || { echo "$THEME/config.json is not valid JSON" >&2; exit 1; }

"${ADB[@]}" push "$THEME/config.json" "$TMP-config.json" >/dev/null
put config.json
revision
restart
echo "applied $(basename "$THEME") to ${ANDROID_SERIAL:-the emulator}"
