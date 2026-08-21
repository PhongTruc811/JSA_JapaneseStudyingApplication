# Third-party notices

## MP3SPI

Mizuki Music Mode uses MP3SPI `1.9.5.4` to add MP3 decoding to Java Sound.
MP3SPI is distributed under the GNU Lesser General Public License 2.1.

- Project: http://www.javazoom.net/mp3spi/mp3spi.html
- Maven artifact: `com.googlecode.soundlibs:mp3spi:1.9.5.4`
- License: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

The dependency remains a separate JAR in the packaged application's `lib`
directory. Its transitive JLayer and Tritonus components are copied by the
same Maven runtime-dependency packaging step.
