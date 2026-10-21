#!/usr/bin/env bash
# Create a release signing keystore for the GitHub Actions workflow, then print
# the base64 blob to add as the ANDROID_KEYSTORE_BASE64 secret.
#
# Usage (run from the repository root, or set KEYS_PASS first):
#   KEYS_PASS='change-me-strong-password' bash tools/create_release_keystore.sh
#
# IMPORTANT:
#   * Never commit touchlab-release.keystore or *.b64 files (they are git-ignored).
#   * Keep the password and key alias for the Google Play upload later.
set -euo pipefail

KEYSTORE="touchlab-release.keystore"
ALIAS="touchlab"
KEYS_PASS="${KEYS_PASS:?Set KEYS_PASS to a strong password for this keystore}"

if [ -f "$KEYSTORE" ]; then
  echo "Keystore already exists at $KEYSTORE — remove it first if you want to regenerate."
  exit 1
fi

keytool -genkeypair -v \
  -keystore "$KEYSTORE" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 2048 -validity 10950 \
  -storepass "$KEYS_PASS" \
  -keypass "$KEYS_PASS" \
  -dname "CN=TouchLab, OU=TouchLab, O=TouchLab, L=Unknown, ST=Unknown, C=CN"

echo
echo "=== Add these as GitHub Actions secrets (Settings -> Secrets and variables -> Actions) ==="
echo "ANDROID_KEYSTORE_BASE64:"
base64 -w0 "$KEYSTORE"
echo
echo "ANDROID_KEYSTORE_PASSWORD: $KEYS_PASS"
echo "ANDROID_KEY_ALIAS: $ALIAS"
echo "ANDROID_KEY_PASSWORD: $KEYS_PASS"
echo
echo "Local sanity check (signed APK):"
export ANDROID_KEYSTORE_PATH="$(pwd)/$KEYSTORE"
export ANDROID_KEYSTORE_PASSWORD="$KEYS_PASS"
export ANDROID_KEY_ALIAS="$ALIAS"
export ANDROID_KEY_PASSWORD="$KEYS_PASS"