import 'package:flutter/services.dart';
import 'package:get/get.dart';

class RFIDController extends GetxController {
  static const MethodChannel _methodChannel = MethodChannel('com.example.scanna/method');
  static const EventChannel _eventChannel = EventChannel('com.example.scanna/events');

  // Observable properties
  var isInitialized = false.obs;
  var isReading = false.obs;
  var powerLevel = 0.obs;
  var tagData = <String>[].obs;

  // Listener for tag data
  RxString lastTag = ''.obs;

  @override
  void onInit() {
    super.onInit();
    initializeReader();
    listenToTagReads();
  }

  /// Initialize the RFID reader
  Future<void> initializeReader() async {
    try {
      final result = await _methodChannel.invokeMethod<bool>('initializeReader');
      isInitialized.value = result ?? false;
      if (!isInitialized.value) {
        Get.snackbar('Error', 'Failed to initialize the RFID reader.');
      } else {
        Get.snackbar('Success', 'RFID reader initialized.');
      }
    } catch (e) {
      Get.snackbar('Error', 'Initialization failed: $e');
    }
  }

  /// Start reading RFID tags
  Future<void> startReading() async {
    if (!isInitialized.value) {
      Get.snackbar('Error', 'Reader is not initialized.');
      return;
    }

    try {
      await _methodChannel.invokeMethod('startReading');
      isReading.value = true;
      Get.snackbar('Reading Started', 'RFID reader is now scanning.');
    } catch (e) {
      Get.snackbar('Error', 'Failed to start reading: $e');
    }
  }

  /// Stop reading RFID tags
  Future<void> stopReading() async {
    try {
      await _methodChannel.invokeMethod('stopReading');
      isReading.value = false;
      Get.snackbar('Reading Stopped', 'RFID reader has stopped scanning.');
    } catch (e) {
      Get.snackbar('Error', 'Failed to stop reading: $e');
    }
  }

  /// Set power level of the RFID reader
  Future<void> setPower(int level) async {
    try {
      final result = await _methodChannel.invokeMethod<bool>('setAntennaPower', {'power': level});
      if (result == true) {
        powerLevel.value = level;
        Get.snackbar('Success', 'Power level set to $level.');
      } else {
        Get.snackbar('Error', 'Failed to set power level.');
      }
    } catch (e) {
      Get.snackbar('Error', 'Failed to set power level: $e');
    }
  }

  /// Get current power level of the RFID reader
  Future<void> getPower() async {
    try {
      final int result = await _methodChannel.invokeMethod<int>('getAntennaPower') ?? 0;
      powerLevel.value = result;
    } catch (e) {
      Get.snackbar('Error', 'Failed to get power level: $e');
    }
  }

  /// Listen to tag reads from the EventChannel
  void listenToTagReads() {
    _eventChannel.receiveBroadcastStream().listen(
          (dynamic data) {
        tagData.add(data.toString());
        lastTag.value = data.toString();
      },
      onError: (error) {
        Get.snackbar('Error', 'Failed to receive tag data: $error');
      },
    );
  }
}
