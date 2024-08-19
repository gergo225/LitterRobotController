#include <Arduino.h>

/*********
  Rui Santos
  Complete instructions at https://RandomNerdTutorials.com/esp32-ble-server-client/
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files.
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
*********/

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>

//BLE server name
#define bleServerName "Litter Robot Motor"

bool deviceConnected = false;

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/
#define SERVICE_UUID "91bad492-b950-4226-aa2b-4ede9fa42f59"
#define CHARACTERISTIC_UUID "ca73b3ba-39f6-4ab3-91ae-186dc9577d99"

// Humidity Characteristic and Descriptor
BLECharacteristic motorCharacteristics(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_WRITE);
BLEDescriptor motorDescriptor(BLEUUID((uint16_t)0x2902));

//Setup callbacks onConnect and onDisconnect
class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    Serial.println("Client connected");
  };
  void onDisconnect(BLEServer* pServer) {
    Serial.println("Client disconnected");
  }
};

class MotorCharacteristicsCallbacks: public BLECharacteristicCallbacks {
	void onWrite(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param) {
    const String value = pCharacteristic->getValue().c_str();

    Serial.print("Got value from client: ");
    Serial.println(value);
  }
};

void setup() {
  // Start serial communication 
  Serial.begin(9600);

  // Create the BLE Device
  BLEDevice::init(bleServerName);

  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *motorService = pServer->createService(SERVICE_UUID);

  // Humidity
  motorService->addCharacteristic(&motorCharacteristics);
  motorDescriptor.setValue("Motor Control");
  motorCharacteristics.addDescriptor(&motorDescriptor);
  motorCharacteristics.setCallbacks(new MotorCharacteristicsCallbacks());
  
  // Start the service
  motorService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pServer->getAdvertising()->start();
  Serial.println("Waiting a client connection to connect...");
}

void loop() {
}