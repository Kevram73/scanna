import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/rfid_controller.dart';

class RFIDScreen extends StatelessWidget {
  final RFIDController rfidController = Get.put(RFIDController());

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('RFID Reader'),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
              onPressed: () => rfidController.initializeReader(),
              child: const Text('Initialize Reader'),
            ),
            Obx(() => Text(rfidController.isInitialized.value
                ? 'Reader Initialized'
                : 'Reader Not Initialized')),
            Obx(() => rfidController.isReading.value
                ? ElevatedButton(
              onPressed: () => rfidController.stopReading(),
              child: const Text('Stop Reading'),
            )
                : ElevatedButton(
              onPressed: () => rfidController.startReading(),
              child: const Text('Start Reading'),
            )),
            ElevatedButton(
              onPressed: () async {
                await rfidController.setPower(30);
                await rfidController.getPower();
              },
              child: const Text('Set Power Level'),
            ),
            Obx(() => Text('Power Level: ${rfidController.powerLevel.value}')),
            const SizedBox(height: 16),
            Obx(() => Text('Last Tag: ${rfidController.lastTag.value}')),
            const Divider(),
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
