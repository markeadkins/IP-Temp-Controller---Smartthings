#include <DallasTemperature.h>
#include <OneWire.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <EEPROM.h>

#define RELAY_BUS 4 // Relay pin#
#define ONE_WIRE_BUS 2 // DS18B20 pin#
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature DS18B20(&oneWire);
ESP8266WebServer server(80);

const char* ssid = "";
const char* password = "";

const int tempMemIndex = 1;
const int delayTime = 1; //minutes to wait before cooling again

float tempC;
float tempF;
int tempSetPoint;
int tempAllowance = 1;
bool isCooling;
String webString = "";
String strStatus = "";
int newSetTemp;

float oldCycleTime;
float oldTime;
float timeSinceLastCycle;

void handleRoot()
{
  getTemperature();
  
  String displayMessage = "Temperature: ";
         displayMessage += String((int)tempF);
         displayMessage += " - TempSetPoint: ";
         displayMessage += String((int)tempSetPoint);
         displayMessage += " - Status: ";
         if (isCooling == true)
         {
          displayMessage += "Cooling";
         }
         else
         {
          displayMessage += "NotCooling";
         }

  server.send(200, "text/plain", displayMessage);
}

void handleJson()
{
  Serial.print("There are ");
  Serial.print(String(server.headers()));
  Serial.println(" Headers");
  Serial.print("There are ");
  Serial.print(String(server.args()));
  Serial.println(" Args");
  Serial.print("The HTTP Method is: ");
  Serial.println(String(server.method()));
  Serial.print("The URI is :");
  Serial.println(String(server.uri()));
  Serial.print("The WiFi Client is: ");
  Serial.println(String(server.client()));
  
  for (int i = 0; i < server.headers(); i++)
  {
    Serial.print("Header Details: ");
    Serial.print(String(server.headerName(i)));
    Serial.print(": ");
    Serial.println(String(server.header(i)));
  }

  for (int i = 0; i < server.args(); i++)
  {
    Serial.print("Args Details: ");
    Serial.print(String(server.argName(i)));
    Serial.print(": ");
    Serial.println(String(server.arg(i)));
  }
  
  getTemperature();

  if (server.argName(0) == "temp")
  {
    newSetTemp = server.arg(0).toInt();
    EEPROM.write(tempMemIndex, newSetTemp);
    EEPROM.commit();
    tempSetPoint = newSetTemp;
  }

  if (isCooling == true)
  {
    strStatus = "Cooling";
  }
  else
  {
    strStatus = "NotCooling";
  }
  
  webString = "{\"object\":{\"temp\":\"";
  webString += String((int)tempF);
  webString += "\",\"tempSetPoint\":\"";
  webString += String((int)tempSetPoint);
  webString += "\",\"status\":\"";
  webString += String(strStatus);
  webString += "\"}}\"";
  
  server.send(200, "application/json", webString);
}

void handleNotFound(){
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET)?"GET":"POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i=0; i<server.args(); i++){
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
}

void setup() {
  Serial.begin(115200);
  EEPROM.begin(512);
  pinMode(RELAY_BUS, OUTPUT);

  oldCycleTime = 0;
  oldTime = 0;
  timeSinceLastCycle = 0;

  // We start by connecting to a WiFi network
  Serial.println("Connecting to "+String(ssid)); 
  WiFi.begin(ssid, password); 
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print("+");
  }
  Serial.println("");
  Serial.println("WiFi connected. IP address: ");
  Serial.println(WiFi.localIP());  

  server.on("/", handleRoot);
  server.on("/json", handleJson);
  
  server.onNotFound(handleNotFound);

  server.begin();
  
  Serial.println("HTTP server started");
  Serial.flush();
  
}

void loop() {
  server.handleClient();
  if((millis() - oldTime) > 10000) //every 10 seconds
  {
    getTemperature();
    oldTime = millis();
  }
}

void getTemperature() {
    DS18B20.requestTemperatures();
    //tempC = DS18B20.getTempCByIndex(0);
    tempF = DS18B20.getTempFByIndex(0);
    tempSetPoint = EEPROM.read(tempMemIndex);
    if (tempF > (tempSetPoint + tempAllowance))
    {
      setCooling();
    }
    else if (tempF < (tempSetPoint - tempAllowance))
    {
      disableCooling();
    }
    
    Serial.print("Temperature: ");
    Serial.println(tempF);
    Serial.print("TempSetPoint: ");
    Serial.println(tempSetPoint);
    Serial.print("Status:  ");
    if (isCooling == true)
    {
      Serial.print("Cooling");
    }
    else
    {
      Serial.print("Not-Cooling");
    }
    Serial.println("");
    Serial.print("Millis is ");
    Serial.print(millis());
    Serial.print(" - OldTime is ");
    Serial.print(oldTime);
    Serial.print(" - OldCycleTime is ");
    Serial.println(oldCycleTime);
    Serial.println("");

}

void setCooling() {
  timeSinceLastCycle = millis() - oldCycleTime;
  if (!(timeSinceLastCycle < (60000 * delayTime)))
  {
    digitalWrite(RELAY_BUS, 1);
    isCooling = true;
  }
}

void disableCooling() {
  digitalWrite(RELAY_BUS, 0);
  oldCycleTime = millis();
  isCooling = false;
}



