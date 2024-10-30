sleep 10
/usr/bin/mc alias set myminio http://minio:9000 admin password

/usr/bin/mc admin user add myminio providerAccessKeyId providerSecretAccessKey
/usr/bin/mc admin user add myminio consumerAccessKeyId consumerSecretAccessKey

/usr/bin/mc mb myminio/provider
/usr/bin/mc mb myminio/consumer

/usr/bin/mc admin policy create myminio allowall /provider-bucket/bucket-policy.json
/usr/bin/mc admin policy attach myminio allowall --user providerAccessKeyId
/usr/bin/mc admin policy attach myminio allowall --user consumerAccessKeyId

/usr/bin/mc cp /provider-bucket/test-document.txt myminio/provider

exit 0
