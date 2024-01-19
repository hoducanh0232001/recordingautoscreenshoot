import 'dart:async';
import 'dart:io';
import 'dart:ui' as ui;


import 'package:ed_screen_recorder/ed_screen_recorder.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:screenshot/screenshot.dart';
import 'package:image_gallery_saver/image_gallery_saver.dart';




void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData.dark(),
      debugShowCheckedModeBanner: false,
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  ScreenshotController screenshotController = ScreenshotController();
  GlobalKey _globalKey = GlobalKey();
  Timer? timer;
  EdScreenRecorder? screenRecorder;
  Map<String, dynamic>? _response;
  bool inProgress = false;

  @override
  void initState() {
    super.initState();
    screenRecorder = EdScreenRecorder();

  }

  Future<void> startRecord({required String fileName}) async {
    Directory? tempDir = await getDownloadsDirectory();
    String? tempPath = tempDir!.path;
    print(tempPath);
    try {
      var startResponse = await screenRecorder?.startRecordScreen(
        fileName: "Eren",
        dirPathToSave: tempPath,
        audioEnable: true,
      );
      timer = Timer.periodic(Duration(seconds: 5), (Timer t) => captureAndSaveScreenshot());
      setState(() {
        _response = startResponse;
      });
    } on PlatformException {
      kDebugMode
          ? debugPrint("Error: An error occurred while starting the recording!")
          : null;
    }
  }

  Future<void> captureAndSaveScreenshot() async {
    // Chụp ảnh từ widget bên trong Screenshot
    try {
      Uint8List? capturedBytes = await screenshotController.capture();

      if (capturedBytes != null) {
        ui.Image capturedImage = await decodeImageFromList(capturedBytes);
        await ImageGallerySaver.saveImage(capturedBytes, name: 'screenshot', quality: 100);
        print('Captured and saved screenshot.');
      } else {
        print('Capture failed: Image is null');
      }
    } catch (e) {
      print('Capture failed: $e');
    }
  }

  Future<void> stopRecord() async {
    try {
      var stopResponse = await screenRecorder?.stopRecord();
      timer?.cancel();
      setState(() {
        _response = stopResponse;
      });
    } on PlatformException {
      kDebugMode
          ? debugPrint("Error: An error occurred while stopping recording.")
          : null;
    }
  }

  Future<void> pauseRecord() async {
    try {
      await screenRecorder?.pauseRecord();
    } on PlatformException {
      kDebugMode
          ? debugPrint("Error: An error occurred while pause recording.")
          : null;
    }
  }

  Future<void> resumeRecord() async {
    try {
      await screenRecorder?.resumeRecord();
    } on PlatformException {
      kDebugMode
          ? debugPrint("Error: An error occurred while resume recording.")
          : null;
    }
  }
  @override
  void dispose() {
    timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Screenshot(
          controller: screenshotController,
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [

                Text("File: ${(_response?['file'] as File?)?.path}"),
                Text("Status: ${(_response?['success']).toString()}"),
                Text("Event: ${_response?['eventname']}"),
                Text("Progress: ${(_response?['progressing']).toString()}"),
                Text("Message: ${_response?['message']}"),
                Text("Video Hash: ${_response?['videohash']}"),
                Text("Start Date: ${(_response?['startdate']).toString()}"),
                Text("End Date: ${(_response?['enddate']).toString()}"),
                ElevatedButton(
                    onPressed: () => startRecord(fileName: "eren"),
                    child: const Text('START RECORD')),
                ElevatedButton(
                    onPressed: () => resumeRecord(),
                    child: const Text('RESUME RECORD')),
                ElevatedButton(
                    onPressed: () => pauseRecord(),
                    child: const Text('PAUSE RECORD')),
                ElevatedButton(
                    onPressed: () => stopRecord(),
                    child: const Text('STOP RECORD')),
              ],
            ),
          ),
        ),

      ),
    );
  }
}