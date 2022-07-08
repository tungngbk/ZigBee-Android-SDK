# zb-sdk

Dien Quang Smart Zigbee SDK
(get file in zb-sdk\dqsmart-zb-sdk-example\dqsmart-zigbee)

**Add SDK dependencies**
Please add following dependencies to the app/build.gradle script when integrating the SDK into Android Studio project:
* implementation 'androidx.room:room-runtime:2.2.5'
* annotationProcessor 'androidx.room:room-compiler:2.2.5'
* implementation 'com.github.mik3y:usb-serial-for-android:2.2.3'

# Release notes
## Version 1.0.11
* Implement exportZigBeeDevice(macUsb) to get data(String) of all zigbee devices in the database
	* Parameter: macUsb is the mac address of the usb
* Implement importZigBeeDevice(macUsb, data) to import data into database
	* Parameter: 
		* macUsb is the mac address of the usb
		* data is taken from the exportZigBeeDevice(macUsb) function 

## Version 1.0.10
* Change switchVersion(int shortMac, int endpoint) to deviceVersionRequest(int shortMac)
* Implement message report of device version. (Message type = 8)

## Version 1.0.9
* Improved device pairing process.
* Implement switchVersion(int shortMac, int endpoint) to get the current version of the switch
	* Parameter: Expected endpoint = 1 to get the version
* Implement print Lqi of device
* Prevent the device from being deleted from the Zigbee network while the device still wants to rejoin the Zigbee network

## Version 1.0.8
* Fixed crash issue during unexpected message length from Zigbee USB dongle.

## Version 1.0.7
* Fixed addDeviceByModel(device_model, time, callback) only suported device model:
	* DeviceModel.ZB_SMOKE_SENSOR
	* DeviceModel.ZB_SMART_SOCKET
	* DeviceModel.ZB_DOOR_LOCK
* Implement coverControl(int shortMac, int endpoint, int value) to control curtain.
	* Parameter: value 0 (Open/Up), 1 (Close/Down), 2 (Stop)
* Implement lockUnlockControl(int shortMac, int endpoint, int value) to control Yale Lock.
	* Parameter: value = 0 (LOCK) | 1 (UNLOCK)
* Change model_id, should pairing again with button.
	* ZB_AQARA_SINGLE_BUTTON_SENSOR: "0224"; (older: "0231")
	* ZB_AQARA_DUAL_BUTTON_SENSOR: "0225"; (older: ""0232")
* Support:
	* Yale Lock (data: 1 lock, 2 unlock)
	* Curtain (Only control)
	* Socket (data: 1 On, 0 Off)


## Version 1.0.6
* Restart the zigbee network with the USB attached.
* Fixed message report of smoke sensor.
* Implement message report of button.(MSG_TYPE_BUTTON_ACTION)
* Fixed func addDeviceByModel(device_model, time, callback) add only one selected device model. Ex: DeviceModel.ZB_SMOKE_SENSOR

## Version 1.0.5
* Fixed message_type when reported.
* Fixed model_id incorrect when pairing.
* Implement addDeviceByModel(device_model, time, callback) => device_model: get from DeviceModel. Ex: DeviceModel.ZB_RE4_SWITCH

## Version 1.0.4
* Support motion sensor.
* Support door sensor.
* Support temperature and humidity sensor.

## Version 1.0.3
* Support Aqara single and double button sensor.

## Version 1.0.2
* Fixed crash issue when received report from device.
* Fixed unrecognized message issue.

## Version 1.0.1
* Fixed reset factory issue.
