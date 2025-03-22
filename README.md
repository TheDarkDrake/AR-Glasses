# ARGlasses

ARGlasses is an augmented reality application that integrates Bluetooth connectivity, SMS notifications, speech recognition, and translation services. This project is designed to enhance the functionality of AR glasses by providing real-time translations and notifications.

## Table of Contents

- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
- [Permissions](#permissions)
- [Dependencies](#dependencies)
- [ESP32 Firmware](#esp32-firmware)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Bluetooth Connectivity**: Connect to Bluetooth devices like SmartGlass.
- **SMS Notifications**: Receive SMS notifications directly on your AR glasses.
- **Speech Recognition**: Recognize speech using Speech Recognizer
- **Translation**: Translate recognized speech into different languages using LibreTranslate running on localhost.
- **Notification Service**: Listen to and handle notifications from other apps.

## Setup

### Prerequisites

- Android Studio
- Android device with Bluetooth and microphone
- LibreTranslate running on localhost
- Arduino IDE for ESP32 firmware

### Installation

1. **Clone the repository:**

    ```sh
    git clone https://github.com/TheDarkDrake/ARGlasses.git
    cd ARGlasses
    ```

2. **Open the project in Android Studio.**



3. **Ensure LibreTranslate is running on localhost:**

    - Follow the instructions to run LibreTranslate on your local machine.

4. **Build and run the project on your Android device.**

## Usage

1. **Connect to SmartGlass:**

    - Ensure your SmartGlass device is powered on and discoverable.
    - Open the app and it will automatically connect to the SmartGlass device.

2. **Speech Recognition and Translation:**

    - Press the speech button and speak in the selected language.
    - The recognized text will be displayed on the screen.
    - If translation is enabled, the text will be translated to the target language using LibreTranslate on localhost.

3. **Receive SMS Notifications:**

    - Ensure SMS permissions are granted.
    - Incoming SMS messages will be displayed on the screen.

## Permissions

The app requires the following permissions:

- `RECEIVE_SMS`
- `READ_SMS`
- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `BLUETOOTH_CONNECT`
- `RECORD_AUDIO`
- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `RECEIVE_BOOT_COMPLETED`
- `BIND_NOTIFICATION_LISTENER_SERVICE`

## Dependencies

The project uses the following dependencies:

- AndroidX libraries
- Jetpack Compose
- Kotlin Coroutines

## ESP32 Firmware

### Prerequisites

- Arduino IDE
- ESP32 board

### Installation

1. **Install the ESP32 Board in Arduino IDE:**

    - Open Arduino IDE.
    - Go to `File` > `Preferences`.
    - In the "Additional Board Manager URLs" field, add the following URL: `https://dl.espressif.com/dl/package_esp32_index.json`.
    - Go to `Tools` > `Board` > `Boards Manager`.
    - Search for "esp32" and install the "esp32" package.

2. **Open the ESP32 Firmware Sketch:**

    - Navigate to the `ESP32_Firmware` directory in the cloned repository.
    - Open the `.ino` file in Arduino IDE.

3. **Upload the Firmware to ESP32:**

    - Connect the ESP32 board to your computer via USB.
    - Select the correct board and port in Arduino IDE.
    - Click the Upload button to upload the firmware to the ESP32 board.
