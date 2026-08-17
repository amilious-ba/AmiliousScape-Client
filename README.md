# AmiliousScape-Client

Unofficial, community-maintained client based on the 2009-era (revision ~530) RuneScape client.

This is a personal/educational fork intended for use with open-source private server projects such as 2009Scape.  
**Not affiliated with, endorsed by, or connected to Jagex Ltd. in any way.**

## Origins

- Base: [Pazaz/RT4-Client](https://github.com/Pazaz/RT4-Client)
- Further work and plugins: [2009Scape RT4 Client](https://gitlab.com/2009scape/rt4-client)

The original goals of the deobfuscation / modernization effort remain:

- Identify and organize classes, methods, fields, and variables
- Remove remaining obfuscation
- Improve readability and modernize libraries (High DPI, refresh rates, etc.)
- Support existing servers via configurable packet behavior flags

## License

GNU Affero General Public License v3.0 (AGPL-3.0)  
You must keep the project open source and provide corresponding source when distributing binaries or running modified versions as a network service.

## Building & Running

**Requirements**
- Java 8+ (SD mode)
- HD mode on Windows currently works best with Java 15 or lower (JOGL WGL context issue on newer JDKs)
- Gradle (wrapper included)
