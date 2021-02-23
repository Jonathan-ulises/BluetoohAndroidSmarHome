#include <SoftwareSerial.h>
#include <Servo.h>

SoftwareSerial bt(4, 2);

int pinLed = 8;
int pinR = 6;
int pinG = 5;
int pinServo = 11;

Servo myServo;

void setup() {
  Serial.begin(9600);
  bt.begin(9600);

  pinMode(pinLed, OUTPUT);
  pinMode(pinR,   OUTPUT);
  pinMode(pinG,   OUTPUT);

  myServo.attach(pinServo);
  
  myServo.write(0);
}

void loop() {
  

    if (bt.available()) {
      char value;
      value = bt.read();
      switch (value) {
        case 'A': //Encender LED
          digitalWrite(8, HIGH);
          break;
  
        case 'B': //Apagar LED
          digitalWrite(8, LOW);
          break;
  
        case 'C': //Abrir puerta, Encender RGB = R.
          myServo.write(90);
          digitalWrite(pinG, HIGH);
          digitalWrite(pinR, LOW);
          break;
  
        case 'D': //Cerrar puerta, Encender RGB = G.
          myServo.write(0);
          digitalWrite(pinR, HIGH);
          digitalWrite(pinG, LOW);
          break;
  
        default: //No se recivieron datos validos.
          Serial.println("No recibi ningin dato valido...");
      }
    }
}
