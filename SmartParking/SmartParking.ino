#include <Servo.h>
#include <Wire.h>
#include <Adafruit_SSD1306.h>
#include <Adafruit_GFX.h>
#include <FirebaseArduino.h>
#include <ESP8266WiFi.h>

//define Firebase
#define FIREBASE_HOST "firebase_host"
#define FIREBASE_AUTH "api_key"

//define Wifi
#define WIFI_SSID "TP-LINK_F53E"
#define WIFI_PASSWORD "60122421"

//define OLED
#define OLED_WIDTH 128
#define OLED_HEIGHT 64

#define OLED_ADDR 0x3c

Adafruit_SSD1306 display(OLED_WIDTH, OLED_HEIGHT);

Servo servo1,servo2;
int servo1_pin = 16;//D0
int sensor1_pin = 13;//D7
int servo2_pin = 15;//D8
int sensor2_pin = 0;//D3
int sensor1_va1, sensor2_val;

//parking slots
int psensor1_pin = 2; //D4
int psensor2_pin = 14; //D5
int psensor3_pin = 12; //D6

int psensor1_val, psensor2_val, psensor3_val;

void setup(){
  Serial.begin(115200);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);                                  
  Serial.print("Connecting to ");
  Serial.print(WIFI_SSID);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
 
  Serial.println();
  Serial.print("Connected");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());                               //prints local IP address
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH); 

  display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR);
  display.clearDisplay();
  display.setTextSize(2);
  display.setTextColor(WHITE);
  display.setCursor(0,0);
  display.println("Welcome");
  display.println("Smart Parking System");
  display.display();
  display.clearDisplay();
  
  pinMode(servo1_pin,OUTPUT);
  pinMode(sensor1_pin, INPUT);
  servo1.attach(servo1_pin);
  servo1.write(90);
  
  pinMode(servo2_pin, OUTPUT);
  pinMode(sensor2_pin, INPUT);
  servo2.attach(servo2_pin);
  servo2.write(0);

  pinMode(psensor1_pin, INPUT);
  pinMode(psensor2_pin, INPUT);
  pinMode(psensor3_pin, INPUT);
  
  delay(2000);
  
}

void loop(){
  Serial.println("working");
  String fireSlot1, fireSlot2, fireSlot3;
  sensor1_va1 = digitalRead(sensor1_pin);
  sensor2_val = digitalRead(sensor2_pin);
  Serial.write(sensor2_val);
  psensor1_val = digitalRead(psensor1_pin);
  psensor2_val = digitalRead(psensor2_pin);
  psensor3_val = digitalRead(psensor3_pin);
  
  if((sensor1_va1) || (!psensor1_val && !psensor2_val && !psensor3_val) ){
    servo1.write(90);
  }
  else {
    servo1.write(0);
  }
  if(sensor2_val){
    servo2.write(0);
  }
  else{
    servo2.write(90);
  }
 
 if(!psensor1_val && !psensor2_val && !psensor3_val){
    display.clearDisplay();
    display.setCursor(0,0);
    display.println("No Slots!!");
    display.display();
 }
 else{
    display.clearDisplay();
    display.setCursor(0,0);
    display.println("Available");
    display.display();
 }
  
  //First Slot Checking
  if (!psensor1_val)
  {
    //display.clearDisplay();
    fireSlot1 = "Occupied";
    display.print("S1:N ");
    display.display();

  } 
  else {
    //display.clearDisplay();
    fireSlot1 = "Vacant";
    display.print("S1:A ");
    display.display();

  }
  delay(1000);

  //Second Slot Checking
  if (!psensor2_val)
  {
    fireSlot2 = "Occupied";
    display.print("S2:N ");
    display.display();
  } 
  else {
    fireSlot2 = "Vacant";
    display.print("S2:A ");
    display.display();

  }
  delay(1000);
  
  //Third Slot Checking
  if (!psensor3_val)
  {
    fireSlot3 = "Occupied";
    display.print("S3:N ");
    display.display();

  } 
  else{
    fireSlot3 = "Vacant";
    display.print("S3:A ");
    display.display();
  }
  
  Firebase.setString("/PL1/s1", fireSlot1);
  Firebase.setString("/PL1/s2", fireSlot2);
  Firebase.setString("/PL1/s3", fireSlot3);
  
   if (Firebase.failed()) 
    {
 
      Serial.print("pushing /logs failed:");
      Serial.println(Firebase.error()); 
      return;
  }
  delay(1000);
}

