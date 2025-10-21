#include <ESP_8_BIT_GFX.h>
#include <BluetoothSerial.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "esp_bt_main.h"
#include "esp_bt_device.h"
#include "esp_bt.h"
#include "esp32-hal-bt.h"
#include <ArduinoJson.h>
#include <vector>
#include <ESP32Time.h>
#include <SPI.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <WiFi.h>

// WiFi Credentials (Change these values accordingly)
const char* WIFI_SSID = "Username";
const char* WIFI_PASSWORD = "password";

// APA102 Dotstar LED Pins (Updated for ESP32-PICO-KIT V4)
#define DOTSTAR_PWR 26  // Adjusted for ESP32-PICO-KIT V4
#define DOTSTAR_DATA 27
#define DOTSTAR_CLK 25

// Battery Monitoring Pins (Updated for ESP32-PICO-KIT V4)
#define BAT_CHARGE 32
#define BAT_VOLTAGE 33

// OLED Display Pins (Waveshare 1.51-inch OLED on SPI)
#define OLED_MOSI 23  // DIN (MOSI)
#define OLED_CLK 18   // CLK (SCK)
#define OLED_CS 5     // Chip Select
#define OLED_DC 2     // Data/Command Select
#define OLED_RST 4    // Reset (optional)

#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 64 // OLED display height, in pixels

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &SPI, OLED_DC, OLED_RST, OLED_CS);

BluetoothSerial SerialBT;

String receivedText = "";

void setup() {
    Serial.begin(115200);
    SerialBT.begin("SmartGlass"); // Bluetooth device name
    
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\nWiFi Connected!");

    // Initialize OLED display
    if (!display.begin(SSD1306_SWITCHCAPVCC)) {
        Serial.println("SSD1306 allocation failed");
        for (;;);
    }
    display.clearDisplay();
    display.setRotation(2);  // Invert the display by 180 degrees
    display.setTextSize(1);
    display.setTextColor(WHITE);
    display.setCursor(0, 10);
    display.println("SmartGlass Ready!");
    display.display();
}

void loop() {
    // 🔋 Read battery voltage
    int batteryLevel = analogRead(BAT_VOLTAGE);
    float voltage = (batteryLevel / 4095.0) * 3.3;  // Assuming a 3.3V reference

    // 🎤 Check if data is received from Bluetooth
    if (SerialBT.available()) {
        receivedText = SerialBT.readStringUntil('\n');  // Read incoming text
        Serial.print("Received: ");
        Serial.println(receivedText);
    }

    // 📺 Update OLED Display
    static String lastText = "";
    if (receivedText != lastText) {
        lastText = receivedText;
        display.clearDisplay();
        display.setTextSize(1);
        display.setTextColor(SSD1306_WHITE);

        // ✅ Show "Connected" on the top left
        display.setCursor(0, 0);
        display.print("Connected");

        // 🔋 Show Battery Level on the top right
        display.setCursor(90, 0);
        display.print("Batt: ");
        display.print(voltage, 2);  // Updated battery voltage

        // 🖥 Show received text in the middle
        display.setCursor(10, 30);
        display.print(receivedText);
        display.display();  // Update OLED Display
    }

    // 📶 Show Wi-Fi status at the bottom
    display.setCursor(0, 50);
    if (WiFi.status() == WL_CONNECTED) {
        display.print("Wi-Fi: Connected");
    } else {
        display.print("Wi-Fi: Failed");
    }

    delay(500);  // Small delay to avoid flickering
}