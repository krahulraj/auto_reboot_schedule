import 'package:flutter/services.dart';

class KioskModeHelper {
  static const MethodChannel _channel = MethodChannel('com.example.device_auto_launch/native');

  static Future<void> rebootDevice(String time) async {
    try {
      await _channel.invokeMethod('rebootDevice',{'time':time});
    } on PlatformException catch (e) {
      print("Failed to reboot device: ${e.message}");
    }
  }

  static Future<void> sleepDevice() async{
    try{
      await _channel.invokeListMethod('sleep');
    }
    catch(e){
      print("Exeception occured while making device into sleepmode: ${e.toString()}");
    }
  }

  static Future<void> setAlarm() async {
    try {
      await _channel.invokeMethod('setAlarm');
    } on PlatformException catch (e) {
      print("Failed to reboot device: ${e.message}");
    }
  }

  static Future<void> lockTheDevice() async {
    try {
      await _channel.invokeMethod('lockTheDevice');
    } on PlatformException catch (e) {
      print("Failed to enable kiosk mode: ${e.message}");
    }
  }
}
