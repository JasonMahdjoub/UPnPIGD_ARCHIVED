UPnP IGD
========

[![CodeQL](https://github.com/JasonMahdjoub/UPnPIGD/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/JasonMahdjoub/MaDKitLanEdition/actions/workflows/codeql-analysis.yml)

This is a fork of UPnPIGD, the UPnP stack for Java and Android
------------------------------------------------------------

The fork fix some security issues, and upgrade used libraries. Efforts were only made into UPNP IGD part. The project's goals are strict specification compliance, complete, clean and extensive APIs, as well as rich SPIs for easy customization.

UPnPIGD is Free Software, distributed under the terms of the <a href="https://www.gnu.org/licenses/lgpl-2.1.html">GNU Lesser General Public License</a> <b>or at your option</b> the <a href="https://opensource.org/licenses/CDDL-1.0">Common Development and Distribution License</a>.

How to use it ?
---------------
### With Gradle :

Adapt into your build.gradle file, the next code :

```
	...
	dependencies {
		...
		api(group:'com.distrimind.upnp_igd', name: 'UPnPIGD-Core', version: '1.1.0-STABLE')
		...
	}
	...
```	
When using UPnPIGD into Android, if you want to use UPnPIGD functions, please use this additionnal dependencies (minimum Java version is 11) :
```
	...
	dependencies {
		...
		implementation(group:'com.distrimind.upnp_igd', name: 'UPnPIGD-Android', version: '1.1.0-STABLE')
		...
	}
	...
```	
Librairies are available on Maven Central. You can check signatures of dependencies with this [public GPG key](key-2023-10-09.pub). You can also use the next repository : 
```
	...
	repositories {
		...
		maven {
	       		url "https://artifactory.distri-mind.fr:443/artifactory/gradle-release/"
	   	}
		...
	}
	...
```
To know what is the last updaloed version, please refer to versions availables here : [this repository](https://artifactory.distri-mind.fr/artifactory/DistriMind-Public/com/distrimind/upnp_igd/UPnPIGD/)
### With Maven :
Adapt into your pom.xml file, the next code :
```
	...
	<project>
		...
		<dependencies>
			...
			<dependency>
				<groupId>com.distrimind.upnp_igd</groupId>
				<artifactId>UPnPIGD-Core</artifactId>
				<version>1.1.0-STABLE</version>
			</dependency>
			...
		</dependencies>
		...
	</project>
	...
```
When using UPnPIGD into Android, if you want to use UPnPIGD functions, please use this additionnal dependencies (minimum Java version is 11) :
```
	...
    <dependency>
        <groupId>com.distrimind.upnp_igd</groupId>
        <artifactId>UPnPIGD-Android</artifactId>
        <version>1.1.0-STABLE</version>
    </dependency>
	...
```
Librairies are available on Maven Central. You can check signatures of dependencies with this [public GPG key](key-2023-10-09.pub). You can also use the next repository : 
```
	...
	<repositories>
		...
		<repository>
			<id>DistriMind-Public</id>
			<url>https://artifactory.distri-mind.fr:443/artifactory/gradle-release/</url>
		</repository>
		...
	</repositories>
	...		
```
To know what last version has been uploaded, please refer to versions availables into [this repository](https://artifactory.distri-mind.fr/artifactory/DistriMind-Public/com/distrimind/upnp_igd/UPnPIGD/)


