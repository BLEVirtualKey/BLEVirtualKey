echo "generating the Install TA operation"

ADMIN_TOOL=../../common/ta/cmdgen
TA_DATA_PATH=../../common/ta/property

java -jar $ADMIN_TOOL/AdminCmdGenerator-1.2.jar genAdminCmd \
-commandID "0x10001" \
-authorizingSD "103A6BC4-4F5B-B087-9CDA-0E9DE465758F" \
-taID  "$3" \
-tsdID "103A6BC4-4F5B-B087-9CDA-0E9DE465758F" \
-initialState "0x01" \
-inputTABin "$1/$3.bin" \
-inputTAProperties "$TA_DATA_PATH/$3.txt" \
-inputMetadataGH "$TA_DATA_PATH/GH.metadata" \
-outputCmd "$2/$3.com" \
-outputTABinUploadAppData "$2/$3.appdata"
