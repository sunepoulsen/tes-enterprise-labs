#!/bin/bash

ALIAS=tes-enterprise-labs

KEY_ALGORITHM=RSA
KEY_SIZE_IN_BITS=2048
KEYSTORE_TYPE=PKCS12
KEYSTORE_FILE=$ALIAS.p12
PEM_FILE=$ALIAS.pem
KEY_FILE=$ALIAS.key

CERTIFICATE_VALIDITY_IN_DAYS=365

CN="Sune Thomas Poulsen"
ORGANISATION="Private Person"
COMPANY="Private Person"
CITY="Copenhagen"
STATE="Region Hovedstaden"
COUNTRY="DK"
CERTIFICATE_DISTINGUISHED_NAME="CN=$CN, OU=$ORGANISATION, O=$COMPANY, L=$CITY, ST=$STATE, C=$COUNTRY"

SUBJECT_ALTERNATIVE_NAME="SAN=dns:localhost,ip:127.0.0.1"

mkdir -p $1
cd $1

# Delete previous certificate files
rm -rf $ALIAS-password.txt $KEYSTORE_FILE $PEM_FILE $KEY_FILE

# Generate a passphrase for certificate
openssl rand -base64 48 > $ALIAS-password.txt
KEYSTORE_PASSWORD=$(cat $ALIAS-password.txt)

keytool -genkeypair -alias $ALIAS -keyalg $KEY_ALGORITHM -keysize $KEY_SIZE_IN_BITS -storetype $KEYSTORE_TYPE -validity $CERTIFICATE_VALIDITY_IN_DAYS -keystore $KEYSTORE_FILE -storepass $KEYSTORE_PASSWORD -keypass $KEYSTORE_PASSWORD -dname "$CERTIFICATE_DISTINGUISHED_NAME" -ext "$SUBJECT_ALTERNATIVE_NAME"

echo "Extract $PEM_FILE"
openssl pkcs12 -in $KEYSTORE_FILE -passin pass:$KEYSTORE_PASSWORD -passout pass:$KEYSTORE_PASSWORD -out $PEM_FILE -nodes
chmod ug+rw $PEM_FILE
chmod o+r $PEM_FILE

echo "Extract $KEY_FILE"
openssl pkcs12 -in $KEYSTORE_FILE -passin pass:$KEYSTORE_PASSWORD -passout pass:$KEYSTORE_PASSWORD -nocerts -out $KEY_FILE
chmod ug+rw $KEY_FILE
chmod o+r $KEY_FILE

cd -
