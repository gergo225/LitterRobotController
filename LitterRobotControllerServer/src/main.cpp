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
#include "AiEsp32RotaryEncoder.h"

#define PIN_IN1  19 // ESP32 pin GPIO19 connected to the IN1 pin L298N
#define PIN_IN2  18 // ESP32 pin GPIO18 connected to the IN2 pin L298N
#define PIN_ENA  17 // ESP32 pin GPIO17 connected to the EN1 pin L298N
#define MAX_SPEED 255

#define ROTARY_ENCODER_CLK 25   // ESP32 pin GPIO4 connected to the CLK pin on the KY-040 rotary encoder
#define ROTARY_ENCODER_DT 26    // ESP32 pin GPIO2 connected to the DT pin on the KY-040 rotary encoder
#define ROTARY_ENCODER_SW 27    // ESP32 pin GPIO15 connected to the SW pin on the KY-040 rotary encoder
#define ROTARY_ENCODER_STEPS 2

AiEsp32RotaryEncoder rotaryEncoder = AiEsp32RotaryEncoder(ROTARY_ENCODER_DT, ROTARY_ENCODER_CLK, ROTARY_ENCODER_SW, -1, ROTARY_ENCODER_STEPS);

enum MotorState {
  STOPPED,
  ROTATE_LEFT,
  ROTATE_RIGHT
};

MotorState motorState = STOPPED;
MotorState previousMotorState = STOPPED;

//BLE server name
#define bleServerName "Litter Robot Motor"

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

// Setup callback onWrite (value from client)
class MotorCharacteristicsCallbacks: public BLECharacteristicCallbacks {
	void onWrite(BLECharacteristic* pCharacteristic, esp_ble_gatts_cb_param_t* param) {
    const String value = pCharacteristic->getValue().c_str();

    Serial.print("Got value from client: ");
    Serial.println(value);
    updateMotorStateUsingString(value);
  }

  void updateMotorStateUsingString(String stateString) {
    stateString.toLowerCase();
    if (stateString == "stop") {
      motorState = STOPPED;
    } else if (stateString == "left") {
      motorState = ROTATE_LEFT;
    } else if (stateString == "right") {
      motorState = ROTATE_RIGHT;
    }
  }
};

void stopMotor() {
  Serial.println("Motor: stop");
  digitalWrite(PIN_ENA, 0);
  digitalWrite(PIN_IN1, LOW);
  digitalWrite(PIN_IN2, LOW);
}

void rotateMotorLeft() {
  Serial.println("Motor: rotate left (anti-clockwise)");
  analogWrite(PIN_ENA, MAX_SPEED);
  digitalWrite(PIN_IN1, LOW);
  digitalWrite(PIN_IN2, HIGH);
}

void rotateMotorRight() {
  Serial.println("Motor: rotate right (clockwise)");
  analogWrite(PIN_ENA, MAX_SPEED);
  digitalWrite(PIN_IN1, HIGH);
  digitalWrite(PIN_IN2, LOW);
}

void processMotorState() {
  if (motorState == previousMotorState) {
    return;
  }
  previousMotorState = motorState;

  switch (motorState) {
    case STOPPED:
      stopMotor();
      break;
    case ROTATE_LEFT:
      rotateMotorLeft();
      break;
    case ROTATE_RIGHT:
      rotateMotorRight();
      break;
  }
}

void IRAM_ATTR readEncoderISR() {
  rotaryEncoder.readEncoder_ISR();
}

void setupBLE() {
  // initialize digital pins as outputs.
  pinMode(PIN_IN1, OUTPUT);
  pinMode(PIN_IN2, OUTPUT);
  pinMode(PIN_ENA, OUTPUT);

  // Create the BLE Device
  BLEDevice::init(bleServerName);

  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *motorService = pServer->createService(SERVICE_UUID);

  // Create BLE Characteristic
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

void setupRotaryEncoder() {
  rotaryEncoder.begin();
  rotaryEncoder.setup(readEncoderISR);
  rotaryEncoder.disableAcceleration();
}

void processRotaryEncoderChange() {
  int encoderChange = rotaryEncoder.encoderChanged();

  if (encoderChange) {
    bool isClockwise = encoderChange > 0;

    if (motorState == STOPPED) {
      if (isClockwise) {
        Serial.println("Rotary knob: changing motor state (clockwise)");
      } else {
        Serial.println("Rotary knob: changing motor state (anti-clockwise)");
      }

      motorState = isClockwise ? ROTATE_RIGHT : ROTATE_LEFT;
    } else {
      Serial.println("Rotary knob: can't change motor state until it's 'STOPPED'");
    }
  }

  if (rotaryEncoder.isEncoderButtonClicked() && motorState != STOPPED) {
    Serial.println("Rotary knob: click (stopping motor)");
    motorState = STOPPED;
  }
}

void setup() {
  // Start serial communication 
  Serial.begin(9600);

  setupRotaryEncoder();
  setupBLE();
}

void loop() {
  processRotaryEncoderChange();
  processMotorState();
  delay(100);
}
