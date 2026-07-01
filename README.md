# Indoor Wi-Fi Positioning System (Android)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)

## Overview
This project implements an indoor localization system for Android devices developed entirely in Java. Since traditional GPS is highly inaccurate or completely ineffective in enclosed environments, this application leverages surrounding Wi-Fi networks to estimate the user's position in real-time by analyzing the Received Signal Strength Indicator (RSSI).

## Key Features
* **Background Wi-Fi Scanning:** Continuous detection of available Access Points (APs) in the environment.
* **Digital Signal Processing (DSP):** Filtering of raw, noisy RSSI signals to stabilize power readings before distance calculation.
* **Multilateration Engine:** Geometric calculation of the user's spatial coordinates using distances estimated from multiple known Access Points.
* **UI Visualization:** Indoor map or floor plan rendering with dynamic updates of the estimated user position.

## Tech Stack & Architecture
* **Language:** Java
* **IDE:** Android Studio
* **Min SDK:** API 30 (Android 11)
* **Required Permissions:**
  * `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` (mandatory for Wi-Fi scanning on Android).
  * `CHANGE_WIFI_STATE` and `ACCESS_WIFI_STATE`.

## How the Algorithm Works
Unlike fingerprinting methods, this application relies on a mathematical propagation model to determine the user's location through the following steps:

1. **Distance Estimation:** The system reads the RSSI from nearby Access Points and converts the signal strength into a physical distance estimate (in meters) using a propagation model (e.g., Log-Distance Path Loss model).
2. **Signal Filtering:** The calculated distances (or the raw RSSI values) are processed through filters to mitigate environmental noise and multipath fading.
3. **Multilateration:** Using the known (X, Y) coordinates of at least three Access Points and their respective estimated distances, the system applies least-squares optimization (or similar geometric intersections) to compute the most probable user position.
*(Note: Update this section if your approach relies on signal propagation formulas to compute distances from individual routers followed by trilateration).*

## Installation & Setup
1. Clone this repository to your local machine (via https):
   ```bash
   git clone https://github.com/GabrieleTrigliaUNIGE/IndoorWiFiPositioning/
2. Clone this repository to your local machine (via ssh):
   ```bash
   git clone git@github.com:GabrieleTrigliaUNIGE/IndoorWiFiPositioning/

## Contributors
This project was co-authored and developed by a team of 4 students:
* **[Gabriele Triglia]**  - [GitHub Profile](https://github.com/GabrieleTrigliaUNIGE)
* **[Alessio Marrazzo]**  - [GitHub Profile](https://github.com/AlessioMarrazzo)
* **[Loris Costanzo]**    - [GitHub Profile](https://github.com/lorisscosta)
* **[Celeste Basso]**     - [GitHub Profile](https://github.com/CeleBasso)

## License
This project is distributed under the GNU General Public License v3.0 (GPL-3.0). See the [LICENSE](LICENSE) file for more information.
