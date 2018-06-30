#include <Time.h>
#include <TimeLib.h>
#include <mthread.h>
#include <Wire.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
#include <dht.h>

// Set the LCD address to 0x27 for a 16 chars and 2 line display
LiquidCrystal_I2C lcd(0x27, 16, 2); // 디스플레이

dht DHT;
#define DHT11_PIN 8 // 온습도 센서

int soil_moisturePin = A2;  //토양 습도 센서

SoftwareSerial BTSerial(0, 1);  // Connect HC-06(블루투스 모듈) TX, RX
//hc-06 TX -> Nano Rx(0)
//hc-06 RX -> Nano Tx(1)

int ledPin = 5;  // led
int ledFlag = 0; // led 작동 여부
boolean ledArray[12];  // led 작동 시간 배열

int motorPin = 3; // 펌프 모터
int motorFlag = 0;  // 펌프 작동 여부
boolean motorArray[12];  // 펌프 작동 시간 배열

char buffer[100];  // 데이터 수신 버퍼
int bufferPosition = 0; // 버퍼에 기록할 위치
String inData = "";    // 받은 데이터
int chk;


// Our custom Thread:
class BioCubeThread : public Thread
{
  public:
    BioCubeThread(int id);
  protected:
    bool loop();
  private:
    int id;
};

BioCubeThread::BioCubeThread(int id)
{
  this->id = id;
}

bool BioCubeThread::loop()
{

  // Die if requested:
  if (kill_flag)
    return false;

  if (id == 1) {  /* lcd에 실시간으로 센서값 띄우는 쓰레드 */
    //Serial.println("lcd");
    // Read sensor value and Display on LCD
    lcd.clear();
    chk = DHT.read11(DHT11_PIN);
    switch (chk)
    {
      case DHTLIB_OK:
        lcd.setCursor(0, 0); // 첫번째줄
        lcd.print(DHT.temperature);
        lcd.print("C / ");
        lcd.print(DHT.humidity);
        lcd.print("%");
        break;
      case DHTLIB_ERROR_CHECKSUM:
        lcd.print("Checksum error,\t");
        break;
      case DHTLIB_ERROR_TIMEOUT:
        lcd.print("Time out error,\t");
        break;
      case DHTLIB_ERROR_CONNECT:
        lcd.print("Connect error,\t");
        break;
      case DHTLIB_ERROR_ACK_L:
        lcd.print("Ack Low error,\t");
        break;
      case DHTLIB_ERROR_ACK_H:
        lcd.print("Ack High error,\t");
        break;
      default:
        lcd.print("Unknown error,\t");
        break;
    }
    lcd.setCursor(0, 1); // 두번째줄
    lcd.print("soil humi:");
    int soil_humi = analogRead(soil_moisturePin);
    lcd.print(soil_humi / 10);
    lcd.print("%");

    /* 현재 온습도, 토양 습도 값 블루투스 전송 */
    String sendData = "TEMPER,";
    sendData.concat(DHT.temperature);
    sendData.concat("C,");
    sendData.concat(DHT.humidity);
    sendData.concat("%,");
    soil_humi = analogRead(soil_moisturePin);
    sendData.concat(soil_humi / 10);
    sendData.concat("%");
    byte tempByte[sendData.length() + 1];
    sendData.getBytes(tempByte, sizeof(tempByte));
    tempByte[sendData.length()] = '\n';
    Serial.write(tempByte, sizeof(tempByte));
    BTSerial.write(tempByte, sizeof(tempByte));

    // Sleep for two second:
    sleep(2);
    return true;
  }
  else if (id == 2) { /* 블루투스 통신 쓰레드 */
    /* 블루투스로 데이터 수신 */
    if (BTSerial.available()) { // 블루투스에서 신호가 있으면
      char data = BTSerial.read();
      buffer[bufferPosition++] = data;

      if (data == '\n') {
        buffer[bufferPosition] = '\0';
        for (int i = 0 ; i < bufferPosition - 1; i++) {
          inData.concat(buffer[i]);
        }
        //Serial.println(inData);
        if (inData.length() > 7) { // connect, read_data, set_time
          if ((inData.substring(0, 7)).equals("connect") == 1) {  // 폰과 블루투스 연결
            // 현재 시간 설정(HH,mm,ss,dd,MM,YY)
            setTime(inData.substring(16, 18).toInt(), inData.substring(19, 21).toInt(), inData.substring(22, 24).toInt(), inData.substring(13, 15).toInt(), inData.substring(10, 12).toInt(), inData.substring(7, 9).toInt());

            // led와 모터 현재 상태와 시간 제어 설정되어 있는 시간들 블루투스 전송
            String sendData = "ONTIME,";
            if (ledFlag == 1) { // led 상태 확인
              sendData.concat("ON ");
            } else {
              sendData.concat("OFF ");
            }
            for (int i = 0; i < 12; i++) { // led array 확인
              if (ledArray[i] == true) {
                sendData.concat(i * 2);
                sendData.concat(" ");
              }
            }
            if (motorFlag == 1) { // motor 상태 확인
              sendData.concat(",ON ");
            } else {
              sendData.concat(",OFF ");
            }
            for (int i = 0; i < 12; i++) { // motor array 확인
              if (motorArray[i] == true) {
                sendData.concat(i * 2);
                sendData.concat(" ");
              }
            }
            byte tempByte[sendData.length() + 1];
            sendData.getBytes(tempByte, sizeof(tempByte));
            tempByte[sendData.length()] = '\n';
            Serial.write(tempByte, sizeof(tempByte));
            BTSerial.write(tempByte, sizeof(tempByte));
          }
          else if ((inData.equals("read_data") == 1)) { // 일지 작성의 '센서값 읽기'
            //대기 온습도, 토양 습도 값 전송
            if (chk == DHTLIB_OK) {
              String sendData = "DIARY,";
              sendData.concat(DHT.temperature);
              sendData.concat("C,");
              sendData.concat(DHT.humidity);
              //Serial.println(tempString);
              sendData.concat("%,");
              sendData.concat(analogRead(soil_moisturePin)/10);
              sendData.concat("%");
              byte tempByte[sendData.length() + 1];
              sendData.getBytes(tempByte, sizeof(tempByte));
              tempByte[sendData.length()] = '\n';
              Serial.write(tempByte, sizeof(tempByte));
              BTSerial.write(tempByte, sizeof(tempByte));
            }
          }
          else if (inData.substring(0, 8).equals("set_time") == 1) {  // 시간 제어 설정
            if (inData.substring(8, 11).equals("led") == 1) { // led
              int receiveTime = inData.substring(11, inData.length()).toInt();
              if (ledArray[receiveTime / 2] == false) {
                ledArray[receiveTime / 2] = true;
              } else {
                ledArray[receiveTime / 2] = false;
              }
            } else {  // pump
              int receiveTime = inData.substring(12, inData.length()).toInt();
              if (motorArray[receiveTime / 2] == false) {
                motorArray[receiveTime / 2] = true;
              } else {
                motorArray[receiveTime / 2] = false;
              }
            }
          }
        } else {  // pump, led, check, ok
          if (inData.equals("pump") == 1) { // 모터 버튼 클릭 한 경우
            //Serial.println("motor execute");
            if (motorFlag == 1) { // 모터 켜져있는 경우 모터 끔
              digitalWrite(motorPin, 0);
              motorFlag = 2;
              byte motor_state[8] = {'P', 'U', 'M', 'P', 'O', 'F', 'F', '\n'};
              Serial.write(motor_state, 8);
              BTSerial.write(motor_state, 8);
            }
            else {  // 모터 꺼져있는 경우 모터 켬
              digitalWrite(motorPin, 10);
              motorFlag = 1;
              byte motor_state[7] = {'P', 'U', 'M', 'P', 'O', 'N', '\n'};
              Serial.write(motor_state, 7);
              BTSerial.write(motor_state, 7);
            }
          }
          else if (inData.equals("led") == 1) { // led 버튼 클릭한 경우
            if (ledFlag == 1) { // led 켜져있는 경우 led 끔
              //digitalWrite(ledPin, LOW);
              digitalWrite(ledPin, 0);
              ledFlag = 2;
              byte led_state[7] = {'L', 'E', 'D', 'O', 'F', 'F', '\n'};
              Serial.write(led_state, 7);
              BTSerial.write(led_state, 7);
            }
            else {  // led 꺼져있는 경우 led 켬
              //digitalWrite(ledPin, HIGH);
              digitalWrite(ledPin, 10);
              ledFlag = 1;
              byte led_state[6] = {'L', 'E', 'D', 'O', 'N', '\n'};
              Serial.write(led_state, 6);
              BTSerial.write(led_state, 6);
            }
          }
          else if (inData.equals("check") == 1) { // 큐브 등록시 선택한 큐브 맞는지 확인
            digitalWrite(ledPin, 10);
            ledFlag = 1;
          }
          else if (inData.equals("ok") == 1) {  // 큐브 등록시 선택한 큐브 확인 후 led off
            digitalWrite(ledPin, 0);
            ledFlag = 0;
          }
        }
        bufferPosition = 0;
        inData = "";
      }
    }

    sleep(0.5);
    return true;
  }
  else if (id == 3) { /* 시간 제어 쓰레드 */
    time_t t = now();
    //Serial.print("timer ");
    //Serial.print(hour(t));
    //Serial.print(":");
    //Serial.print(minute(t));
    //Serial.print(":");
    //Serial.println(second(t));
    if (minute(t) == 0) { // 정각
      if (hour(t) % 2 == 0) { // 짝수 시간대
        if (motorArray[hour(t)/2] == true) { // time to on motor
          if (motorFlag == 0) { // On if motor off
            digitalWrite(motorPin, 10);
            motorFlag = 1;
            byte motor_state[7] = {'P', 'U', 'M', 'P', 'O', 'N', '\n'};
            Serial.write(motor_state, 7);
            BTSerial.write(motor_state, 7);
          }
        } else {  // time to off motor
          if (motorFlag == 1) { // Off if motor on
            digitalWrite(motorPin, 0);
            motorFlag = 0;
            byte motor_state[8] = {'P', 'U', 'M', 'P', 'O', 'F', 'F', '\n'};
            Serial.write(motor_state, 8);
            BTSerial.write(motor_state, 8);
          }
        }
        if (ledArray[hour(t)/2] == true) { // time to on led
          if (ledFlag == 0) { // On if led off
            digitalWrite(ledPin, 10);
            ledFlag = 1;
            byte led_state[6] = {'L', 'E', 'D', 'O', 'N', '\n'};
            Serial.write(led_state, 6);
            BTSerial.write(led_state, 6);
          }
        } else {  // time to led off
          if (ledFlag == 1) { // Off if led on
            digitalWrite(ledPin, 0);
            ledFlag = 0;
            byte led_state[7] = {'L', 'E', 'D', 'O', 'F', 'F', '\n'};
            Serial.write(led_state, 7);
            BTSerial.write(led_state, 7);
          }
        }
      }
    } else {  // 홀수 시간대
      if (minute(t) == 59) { // user_off 상태면 off 상태로 바꿔줌
        if (motorFlag == 2) {
          motorFlag = 0;
        }
        if (ledFlag == 2) {
          ledFlag = 0;
        }
      }
    }

    sleep(10);
    return true;
  }
}


void setup()
{
  lcd.begin();

  BTSerial.begin(9600); // 블루투스 모듈 초기화, 블루투스 연결

  pinMode(ledPin, OUTPUT);  // led 핀 출력 설정
  pinMode(motorPin, OUTPUT);  // 모터 핀 출력 설정

  // Initialize the serial connection:
  Serial.begin(9600);
  delay(1000);

  // Create threads and add them to the main ThreadList:
  for (int i = 1; i <= 3; i++)
    main_thread_list->add_thread(new BioCubeThread(i));

}
