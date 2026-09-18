import 'package:flutter/material.dart';

void main() {
  runApp(const AutoPhotoApp());
}

class AutoPhotoApp extends StatelessWidget {
  const AutoPhotoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Auto Photo WhatsApp',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.blue,
        ),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool running = false;

  void startAutomation() {
    setState(() {
      running = true;
    });
  }

  void stopAutomation() {
    setState(() {
      running = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Auto Photo WhatsApp'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Automação de fotos',
              style: TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 30),

            TextField(
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(
                labelText: 'Número do WhatsApp Business',
                hintText: '+55 11 99999-9999',
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 30),

            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(12),
                color: running
                    ? Colors.green.withValues(alpha: 0.15)
                    : Colors.grey.withValues(alpha: 0.15),
              ),
              child: Column(
                children: [
                  Text(
                    running ? 'ATIVO' : 'PARADO',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: running ? Colors.green : Colors.red,
                    ),
                  ),

                  const SizedBox(height: 8),

                  Text(
                    running
                        ? 'Automação em execução'
                        : 'Automação desligada',
                  ),
                ],
              ),
            ),

            const Spacer(),

            SizedBox(
              height: 55,
              child: FilledButton(
                onPressed: running ? null : startAutomation,
                child: const Text(
                  'INICIAR',
                  style: TextStyle(fontSize: 18),
                ),
              ),
            ),

            const SizedBox(height: 12),

            SizedBox(
              height: 55,
              child: FilledButton(
                onPressed: running ? stopAutomation : null,
                style: FilledButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                child: const Text(
                  'STOP',
                  style: TextStyle(fontSize: 18),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}