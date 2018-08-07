echo "generating the Store Data operation for AES secret key"
ADMIN_TOOL=../../common/ta/cmdgen
TA_DATA_PATH=../../common/ta/property

java -jar $ADMIN_TOOL/AdminCmdGenerator-1.2.jar genAdminCmd \
-commandID "0x10015" \
-authorizingSD "103A6BC4-4F5B-B087-9CDA-0E9DE465758F" \
-taorsdID "$2" \
-inputObjID "1" \
-objType "0xA0000010" \
-acf "0x00000021" \
-inputSKObj "$TA_DATA_PATH/secretkeyAES.bin" \
-outputCmd "$1/STORE_SECRETKEY_AES.com"
