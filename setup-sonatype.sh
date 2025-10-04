#!/bin/bash

# Setup script for Sonatype Central publishing
# This script helps you generate GPG keys and provides instructions for GitHub secrets

set -e

echo "🔧 Setting up Sonatype Central publishing for Kai library"
echo "=================================================="

# Check if gpg is installed
if ! command -v gpg &> /dev/null; then
    echo "❌ GPG is not installed. Please install GPG first:"
    echo "   - Ubuntu/Debian: sudo apt-get install gnupg"
    echo "   - macOS: brew install gnupg"
    echo "   - Windows: Download from https://gnupg.org/download/"
    exit 1
fi

echo "✅ GPG is installed"

# Generate GPG key
echo ""
echo "🔑 Generating GPG key for signing artifacts..."
echo "This will create a new GPG key pair for signing your Maven artifacts."

# Create a temporary GPG configuration
TEMP_GNUPG=$(mktemp -d)
export GNUPGHOME="$TEMP_GNUPG"

# Generate GPG key
gpg --batch --gen-key <<EOF
Key-Type: RSA
Key-Length: 4096
Subkey-Type: RSA
Subkey-Length: 4096
Name-Real: NeuraQuant
Name-Email: contact@neuraquant.io
Expire-Date: 0
Passphrase: sonatype123
%no-protection
%commit
EOF

# Get the key ID
KEY_ID=$(gpg --list-secret-keys --keyid-format LONG | grep sec | head -1 | awk '{print $2}' | cut -d'/' -f2)
echo "✅ Generated GPG key with ID: $KEY_ID"

# Export the private key
echo ""
echo "📤 Exporting GPG private key..."
gpg --armor --export-secret-keys --pinentry-mode loopback --passphrase sonatype123 > gpg-private-key.asc

# Export the public key
echo "📤 Exporting GPG public key..."
gpg --armor --export > gpg-public-key.asc

echo ""
echo "🔑 GPG Key Information:"
echo "   Key ID: $KEY_ID"
echo "   Private Key: gpg-private-key.asc"
echo "   Public Key: gpg-public-key.asc"
echo "   Passphrase: sonatype123"

# Clean up temporary directory
rm -rf "$TEMP_GNUPG"

echo ""
echo "📋 Next Steps:"
echo "=============="
echo ""
echo "1. Upload the public key to a keyserver:"
echo "   gpg --send-keys $KEY_ID"
echo ""
echo "2. Add the following secrets to your GitHub repository:"
echo "   - Go to: https://github.com/NeuraQuant/kai/settings/secrets/actions"
echo "   - Add these secrets:"
echo ""
echo "   SONATYPE_USERNAME: [Your Sonatype username]"
echo "   SONATYPE_PASSWORD: [Your Sonatype password/token]"
echo "   GPG_PRIVATE_KEY: [Contents of gpg-private-key.asc file]"
echo "   GPG_PASSPHRASE: sonatype123"
echo ""
echo "3. Upload your public key to Sonatype Central:"
echo "   - Go to: https://s01.oss.sonatype.org/"
echo "   - Log in with your Sonatype credentials"
echo "   - Go to 'Staging Profiles' and find your profile"
echo "   - Click 'Access' and then 'Upload PGP Key'"
echo "   - Upload the gpg-public-key.asc file"
echo ""
echo "4. Test the workflow:"
echo "   - Push a commit to the main branch"
echo "   - Or manually trigger the workflow from GitHub Actions tab"
echo ""
echo "⚠️  Important Security Notes:"
echo "   - Keep your private key secure and never commit it to version control"
echo "   - Consider using a stronger passphrase in production"
echo "   - The generated files contain sensitive information - delete them after setup"
echo ""
echo "✅ Setup complete! Your library will now be published to Sonatype Central on every push to main."