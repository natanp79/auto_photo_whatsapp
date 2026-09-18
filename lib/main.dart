import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:photo_manager/photo_manager.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'photo_service.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final prefs = await SharedPreferences.getInstance();
  final consentAccepted = prefs.getBool('consent_accepted') ?? false;

  runApp(
    AutoPhotoApp(
      consentAccepted: consentAccepted,
    ),
  );
}

class AutoPhotoApp extends StatelessWidget {
  final bool consentAccepted;

  const AutoPhotoApp({
    super.key,
    required this.consentAccepted,
  });

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
      home: consentAccepted
          ? const HomePage()
          : const ConsentPage(),
    );
  }
}

class ConsentPage extends StatefulWidget {
  const ConsentPage({super.key});

  @override
  State<ConsentPage> createState() => _ConsentPageState();
}

class _ConsentPageState extends State<ConsentPage> {
  bool accepted = false;

  Future<void> accept() async {
    if (!accepted) return;

    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('consent_accepted', true);

    if (!mounted) return;

    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => const HomePage(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Configuração inicial'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Automação de fotos',
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'O aplicativo acessará as fotos do dispositivo '
              'para selecionar as 10 imagens mais recentes.',
              style: TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 16),
            const Text(
              'Quando ativada, a automação poderá compartilhar '
              'essas imagens pelo WhatsApp Business configurado.',
              style: TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 16),
            const Text(
              'A automação ocorrerá em ciclos de aproximadamente '
              '2 minutos enquanto estiver ativa.',
              style: TextStyle(fontSize: 16),
            ),
            const Spacer(),
            CheckboxListTile(
              value: accepted,
              onChanged: (value) {
                setState(() {
                  accepted = value ?? false;
                });
              },
              title: const Text(
                'Li e concordo com o funcionamento do aplicativo.',
              ),
              controlAffinity: ListTileControlAffinity.leading,
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 55,
              child: FilledButton(
                onPressed: accepted ? accept : null,
                child: const Text(
                  'CONTINUAR',
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

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const MethodChannel _nativeChannel =
      MethodChannel('auto_photo_whatsapp/native');

  bool running = false;
  bool loadingPhotos = false;

  String phoneNumber = '';

  List<AssetEntity> photos = [];

  @override
  void initState() {
    super.initState();
    loadPhotos();
  }

  Future<void> loadPhotos() async {
    setState(() {
      loadingPhotos = true;
    });

    final result = await PhotoService.getLatestTenPhotos();

    if (!mounted) return;

    setState(() {
      photos = result;
      loadingPhotos = false;
    });
  }

    Future<void> startAutomation() async {
    if (running) return;

    await _nativeChannel.invokeMethod('startAutomation');

    if (!mounted) return;

    setState(() {
      running = true;
    });
  }

  Future<void> stopAutomation() async {
    await _nativeChannel.invokeMethod('stopAutomation');

    if (!mounted) return;

    setState(() {
      running = false;
    });
  }

  Future<void> resetConsent() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('consent_accepted', false);

    if (!mounted) return;

    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => const ConsentPage(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Auto Photo WhatsApp'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            TextField(
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(
                labelText: 'Número do WhatsApp Business',
                hintText: '+55 11 99999-9999',
                border: OutlineInputBorder(),
              ),
              onChanged: (value) {
                phoneNumber = value;
              },
            ),

            const SizedBox(height: 16),

            Row(
              children: [
                const Expanded(
                  child: Text(
                    '10 fotos mais recentes',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                IconButton(
                  onPressed: loadingPhotos ? null : loadPhotos,
                  icon: const Icon(Icons.refresh),
                ),
              ],
            ),

            const SizedBox(height: 8),

            Expanded(
              child: loadingPhotos
                  ? const Center(
                      child: CircularProgressIndicator(),
                    )
                  : photos.isEmpty
                      ? const Center(
                          child: Text(
                            'Nenhuma foto disponível.\n'
                            'Verifique a permissão de fotos.',
                            textAlign: TextAlign.center,
                          ),
                        )
                      : GridView.builder(
                          itemCount: photos.length,
                          gridDelegate:
                              const SliverGridDelegateWithFixedCrossAxisCount(
                            crossAxisCount: 2,
                            crossAxisSpacing: 8,
                            mainAxisSpacing: 8,
                          ),
                          itemBuilder: (context, index) {
                            return FutureBuilder<Uint8List?>(
                              future: photos[index].thumbnailDataWithSize(
                                const ThumbnailSize(500, 500),
                              ),
                              builder: (context, snapshot) {
                                if (!snapshot.hasData) {
                                  return const Center(
                                    child: CircularProgressIndicator(),
                                  );
                                }

                                return ClipRRect(
                                  borderRadius: BorderRadius.circular(10),
                                  child: Image.memory(
                                    snapshot.data!,
                                    fit: BoxFit.cover,
                                  ),
                                );
                              },
                            );
                          },
                        ),
            ),

            const SizedBox(height: 12),

            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
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
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: running ? Colors.green : Colors.red,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    running
                        ? 'Automação em execução'
                        : 'Automação desligada',
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            SizedBox(
              width: double.infinity,
              height: 52,
              child: FilledButton(
                onPressed: running ? null : startAutomation,
                child: const Text('INICIAR'),
              ),
            ),

            const SizedBox(height: 8),

            SizedBox(
              width: double.infinity,
              height: 52,
              child: FilledButton(
                onPressed: running ? stopAutomation : null,
                style: FilledButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                child: const Text('STOP'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
