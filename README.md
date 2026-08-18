# AmiliousScape-Client

Unofficial community client based on the 2009-era (revision ~530) RuneScape client.

This is a personal/educational fork intended for use with open-source private server projects (such as 2009Scape) and for handheld/modern Windows use (including ROG Ally).

**Not affiliated with, endorsed by, or connected to Jagex Ltd. in any way.**

## Origins

- Base: [Pazaz/RT4-Client](https://github.com/Pazaz/RT4-Client)
- Further community work & plugins: [2009Scape RT4 Client](https://gitlab.com/2009scape/rt4-client)

## Requirements

- **Java 11** (recommended and targeted)
- Windows / Linux (JOGL natives included)
- Gradle wrapper is included (no global Gradle install required)

> HD mode on very new JDKs can still hit the classic JOGL WGL context issue on Windows. Java 11 is the sweet spot this fork is built around.

## Building & Running

```bash
git clone https://github.com/amilious-ba/AmiliousScape-Client.git
cd AmiliousScape-Client

# Windows
gradlew.bat run

# Linux / macOS
./gradlew run