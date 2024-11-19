import 'package:flutter/material.dart';
import 'package:get/get.dart';

import '../controllers/rfid_controller.dart';

class RFIDScreen extends StatelessWidget {
  final RFIDController rfidController = Get.put(RFIDController());

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('RFID Reader'),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Obx(() => rfidController.isInitialized.value
                ? Text('Reader Initialized')
                : Text('Reader Not Initialized')),
            Obx(() => rfidController.isReading.value
                ? ElevatedButton(
              onPressed: rfidController.stopReading,
              child: Text('Stop Reading'),
            )
                : ElevatedButton(
              onPressed: rfidController.startReading,
              child: Text('Start Reading'),
            )),
            ElevatedButton(
              onPressed: () async {
                await rfidController.setPower(30);
                await rfidController.getPower();
              },
              child: Text('Set Power Level'),
            ),
            ElevatedButton(
              onPressed: () async {
                await rfidController.setFrequency(5);
              },
              child: Text('Set Frequency'),
            ),
            Obx(() => Expanded(
              child: ListView.builder(
                itemCount: rfidController.tagData.length,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text(rfidController.tagData[index]),
                  );
                },
              ),
            )),
          ],
        ),
      ),
    );
  }
}
