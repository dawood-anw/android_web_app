
Automated UI Profiler  is a performance analysis and monitoring tool for Android Developers,
which can help them to diagnose performance bottlenecks in android apps from end user standpoint 


1.    Source Code Layout

 •	yslow_android — library
                      — sample

 •	library:
 	⁃	contains the main library code for profiling android application. 
 	⁃	AndroidProfiler.java is the main class which exposes the API to be used for profiling.
 	⁃	Refer to the API docs of Android Profiler for more details about the API.

 •	sample:
 	⁃	contains the test application using the profiling library
 	⁃	a simple console app which shows how the profiler works on Android application

2.     Build Instructions

 •	PreRequisites
 	⁃	 Android SDK along with Eclipse IDE(https://developer.android.com/sdk/index.html)
 	⁃	 Configure Eclipse with ADB as per the instructions


 •	Download the code from git into a folder
 	⁃	Open Eclipse and then select File>Import>General>Existing Projects in Workspace
	⁃	Select Next 
	⁃	Set the YSlow_Android folder as the root directory
	⁃	You can see the library and sample projects 
	⁃	Select Finish
	⁃	To compile the library only, right click on the build.xml of library project and run as Ant Build
	⁃	To compile the sample, right click on the build.xml and run as Ant Build. It will compile both the library and test project

3.     Running YSlow_Android Test App

 ⁃	Connect an Android device to your desktop via USB 
 ⁃	Ensure that the device is detected on your PC (adb devices - command)
 ⁃	Now run the test app via eclipse as Java application

Note: We use Tesseract library which internally uses native libraries. We did our best to 
include most of the libraries required for building/running the code independently across 
all platforms. In case you face any issues while using the code please let us know
