# Certs used for developing Car system Apps
alias: carapps
password: carapps

# platform.keystore
# generated from build/target/product/security/platform:
#    openssl pkcs8 -inform DER -nocrypt -in build/target/product/security/platform.pk8 -out platform.key
#    openssl pkcs12 -export -in build/target/product/security/platform.x509.pem -inkey platform.key -name androiddebugkey -out platform.pem -password pass:android
#    keytool -importkeystore -destkeystore platform.keystore -deststorepass  android -srckeystore platform.pem -srcstoretype PKCS12 -srcstorepass  android
alias: androiddebugkey
password: android
