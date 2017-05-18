#!/bin/bash

SUBJECT="/C=US/ST=California/L=San Jose/O=CloudBees/CN=localhost/OU=CDA"
PASSWD=123123

# Basic setup
rm certindex.* cacert.pem serial* server-* client-*
rm -rf private && mkdir private
rm -rf certs && mkdir certs

# Create our localhost test root CA
echo 100001 > serial
touch certindex.txt
openssl req -new -x509 -extensions v3_ca -keyout private/cakey.pem -out cacert.pem -days 365 -config ./openssl.cnf -subj "$SUBJECT-CA" -passout pass:$PASSWD

# Create the server key and cert
openssl req -new -nodes -out server-req.pem -keyout private/server-key.pem -days 365 -config ./openssl.cnf -subj "$SUBJECT-SRV" -passout pass:$PASSWD
# Sign with our test root CA
openssl ca -batch -out server-cert.pem -days 365 -config ./openssl.cnf -passin pass:$PASSWD -infiles server-req.pem

# Create the client key and cert
openssl req -new -nodes -out client-req.pem -keyout private/client-key.pem -days 365 -config ./openssl.cnf -subj "$SUBJECT-CLI" -passout pass:$PASSWD
# Sign with our test root CA
openssl ca -batch -out client-cert.pem -days 365 -config ./openssl.cnf -passin pass:$PASSWD -infiles client-req.pem 
# Client's PKCS12 file
openssl pkcs12 -export -in client-cert.pem -inkey private/client-key.pem -certfile cacert.pem -name "Client" -out client-cert.p12 -passout pass:$PASSWD