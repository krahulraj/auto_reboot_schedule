/*
import 'package:flutter/material.dart';
import 'kiosk_mode_helper.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('Kiosk Mode Example')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
             */
/* ElevatedButton(
                onPressed: () async {
                  await KioskModeHelper.enableKioskMode();
                },
                child: Text('Enable Kiosk Mode'),
              ),*//*

              ElevatedButton(
                onPressed: () async {
                  await KioskModeHelper.rebootDevice();
                },
                child: Text('Reboot Device'),
              ),
            ],
          ),
        ),
      ),
    );
  }

}
*/
import 'dart:async';
import 'dart:io';
import 'package:aws_s3_upload/aws_s3_upload.dart';
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_gallery_saver/image_gallery_saver.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:device_auto_launch/common_constants.dart' as constants;

import 'kiosk_mode_helper.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MyApp());
}

const MethodChannel _channel = MethodChannel('com.example/alarm');

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Camera Capture POC',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: CapturePhotosScreen(),
    );
  }
}

class CapturePhotosScreen extends StatefulWidget {
  const CapturePhotosScreen({Key? key}) : super(key: key);

  @override
  _CapturePhotosScreenState createState() => _CapturePhotosScreenState();
}

class _CapturePhotosScreenState extends State<CapturePhotosScreen> {
  static String? capturedImagePath;
  static int photoCount = 0;
  TextEditingController timeController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _requestPermissions().then((granted) {
      if (granted) {
        _startPeriodicPhotos();
      } else {
        print('Permissions not granted');
      }
    });
  }


  Future<bool> _requestPermissions() async {
    final cameraStatus = await Permission.camera.request();
    print('Camera permission status: ${cameraStatus.isGranted}');
    return cameraStatus.isGranted;
  }
  /*Future<void> _checkDrawOverAppsPermission() async {
    try {
      await _channel.invokeMethod('checkDrawOverAppsPermission');
    } on PlatformException catch (e) {
      print("Failed to check draw over apps permission: '${e.message}'.");
    }
  }*/

  Future<void> _startPeriodicPhotos() async {
    await _capturePhoto();
  }

  Future<void> _capturePhoto() async {
    print('capturePhoto method called');
    CameraController? cameraController;
    try {
      final cameras = await availableCameras();
      print('Cameras available: ${cameras.length}');
      final firstCamera = cameras.first;
      cameraController = CameraController(firstCamera, ResolutionPreset.max);
      await cameraController.initialize();
      print('Camera initialized: ${cameraController.value.isInitialized}');

      if (!cameraController.value.isInitialized) {
        print('Camera not initialized');
        return;
      }

      await cameraController.setFlashMode(FlashMode.off);
      final Directory? extDir = await getExternalStorageDirectory();
      print('External directory: $extDir');
      if (extDir == null) {
        print('Failed to get external storage directory');
        return;
      }

      if (cameraController.value.isTakingPicture) {
        print('Already taking picture');
        return;
      }

      final XFile photo = await cameraController.takePicture();
      print('Photo taken: ${photo.path}');

      final File savedImage = File(photo.path);
      final result = await ImageGallerySaver.saveFile(savedImage.path);
      if(savedImage != null){
      /* String imageUrl =await  uploadMediaToS3(savedImage);
       print("Image url of S3$imageUrl");*/
      }
      print('Image saved result: $result');
      setState(() {
        capturedImagePath = savedImage.path;
        photoCount++;
      });
      print('Image saved: ${savedImage.path}');
    } on CameraException catch (e) {
      print('Error capturing photo: $e');
    } finally {
      await cameraController?.dispose();
      KioskModeHelper.lockTheDevice();
    }
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('New Auto Image Capture'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            capturedImagePath != null
                ? Container(
              height: 300,
                width: 300,
                child: Image.file(File(capturedImagePath!))
            )
                : Container(),
            SizedBox(height: 20),
            Text(
              'Photos captured: $photoCount',
              style: TextStyle(fontSize: 18),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        child: Text('Schedule'),
          onPressed: () async {

            _showScheduleDailog();
          }
      ),
    );
  }
  
  void _showScheduleDailog(){
    String? errorMessage;
    showDialog(context: context, builder: (context){
      return AlertDialog(
       title: Text("Schedule Reboot"),
       content: TextField(
         controller:timeController,
         decoration: InputDecoration(
           labelText: 'Enter time(HH:MM)',
           border: OutlineInputBorder(
             borderRadius: BorderRadius.circular(8)
           ),
           errorText: errorMessage,
         ),
         keyboardType: TextInputType.datetime,
       ),
        actions: [
          TextButton(onPressed:(){
            Navigator.pop(context);
          }, child: Text('Cancel')),
          TextButton(onPressed: ()async{
            if(timeController.text.isEmpty){
              setState(() {
                errorMessage="Please enter time";
              });
            }
           else{
             setState(() {
               errorMessage = null;
             });
              String time = timeController.text.toString();
              Navigator.pop(context);
              await KioskModeHelper.rebootDevice(time);
            }
          }, child: Text('Schedule')),
        ],
      );
    });
  }

  Future<String> uploadMediaToS3(File? file) async {

    String imageUrl = "";
    try {
      String? value = await AwsS3.uploadFile(
        accessKey: constants.accessKey,
        secretKey: constants.secretKey,
        file: File(file!.path),
        bucket: constants.bucket,
        region: constants.region,
        destDir: constants.s3Filefolder,
      );
      if (value != null) {
        imageUrl = value;
      }
    } catch (e) {
      // Fluttertoast.showToast(msg:constants.genericErrorMsg);
      return imageUrl;
    }
    print("IMAGE URLL : $imageUrl");
    return imageUrl;
  }
}
