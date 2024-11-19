import 'package:flutter/services.dart';
import 'package:get/get.dart';

class RFIDController extends GetxController {
  static const _methodChannel = MethodChannel('com.example.scanna/method');
  static const _eventChannel = EventChannel('com.example.scanna/events');

  // Observable properties for state management
  var isInitialized = false.obs;
  var isReading = false.obs;
  var powerLevel = 0.obs;
  var frequency = 0.obs;
  var tagData = <String>[].obs;

  @override
  void onInit() {
    super.onInit();
    initializeReader();
    listenToTagReads();
  }

  /// Initialize the RFID reader
  Future<void> initializeReader() async {
    try {
      final bool result = await _methodChannel.invokeMethod('initialize');
      isInitialized.value = result;
      if (!result) {
        Get.snackbar('Initialization Failed', 'Could not initialize RFID reader.');
      }
    } catch (e) {
      Get.snackbar('Error', 'Failed to initialize: $e');
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
    } catch (e) {
      Get.snackbar('Error', 'Failed to start reading: $e');
    }
  }

  /// Stop reading RFID tags
  Future<void> stopReading() async {
    try {
      await _methodChannel.invokeMethod('stopReading');
      isReading.value = false;
    } catch (e) {
      Get.snackbar('Error', 'Failed to stop reading: $e');
    }
  }

  /// Set power level of the RFID reader
  Future<void> setPower(int level) async {
    try {
      final bool result = await _methodChannel.invokeMethod('setPower', {'power': level});
      if (result) {
        powerLevel.value = level;
        Get.snackbar('Success', 'Power level set to $level.');
      } else {
        Get.snackbar('Error', 'Failed to set power level.');
      }
    } catch (e) {
      Get.snackbar('Error', 'Failed to set power: $e');
    }
  }

  /// Get current power level
  Future<void> getPower() async {
    try {
      final int level = await _methodChannel.invokeMethod('getPower');
      powerLevel.value = level;
    } catch (e) {
      Get.snackbar('Error', 'Failed to get power: $e');
    }
  }

  /// Set frequency of the RFID reader
  Future<void> setFrequency(int freq) async {
    try {
      final bool result = await _methodChannel.invokeMethod('setFrequency', {'frequency': freq});
      if (result) {
        frequency.value = freq;
        Get.snackbar('Success', 'Frequency set to $freq.');
      } else {
        Get.snackbar('Error', 'Failed to set frequency.');
      }
    } catch (e) {
      Get.snackbar('Error', 'Failed to set frequency: $e');
    }
  }

  /// Listen to tag reads from the EventChannel
  void listenToTagReads() {
    _eventChannel.receiveBroadcastStream().listen(
          (dynamic data) {
        tagData.add(data.toString());
      },
      onError: (dynamic error) {
        Get.snackbar('Error', 'Failed to receive tag data: $error');
      },
    );
  }
}
