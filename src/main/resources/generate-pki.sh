#!/bin/bash
echo "Generating PKI certificates for mTLS..."

cd "$(dirname "$0")"

echo "Step 1: Generate Bank Server identity"
keytool -genkeypair -alias bank-server -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore bank-keystore.p12 -validity 3650 -storepass senha123 -keypass senha123 -dname "CN=localhost, OU=IT, O=LegacyBank, L=Bauru, S=SP, C=BR"

echo "Step 2: Generate Gateway Client identity"
keytool -genkeypair -alias gateway-client -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore gateway-keystore.p12 -validity 3650 -storepass senha123 -keypass senha123 -dname "CN=isogateway, OU=Engineering, O=MyFintech, L=Bauru, S=SP, C=BR"

echo "Step 3: Export Bank public certificate"
keytool -export -alias bank-server -keystore bank-keystore.p12 -storetype PKCS12 -storepass senha123 -file bank.crt

echo "Step 4: Export Gateway public certificate"
keytool -export -alias gateway-client -keystore gateway-keystore.p12 -storetype PKCS12 -storepass senha123 -file gateway.crt

echo "Step 5: Create Gateway truststore with Bank certificate"
keytool -import -alias bank-server -file bank.crt -keystore gateway-truststore.jks -storetype JKS -storepass senha123 -noprompt

echo "Step 6: Create Bank truststore with Gateway certificate"
keytool -import -alias gateway-client -file gateway.crt -keystore bank-truststore.jks -storetype JKS -storepass senha123 -noprompt

echo "Cleaning up..."
rm -f bank.crt gateway.crt

echo "PKI generation complete!"
echo "Files created:"
echo "  - gateway-keystore.p12 (Gateway identity)"
echo "  - gateway-truststore.jks (Gateway trusts Bank)"
echo "  - bank-keystore.p12 (Bank identity)"
echo "  - bank-truststore.jks (Bank trusts Gateway)"
