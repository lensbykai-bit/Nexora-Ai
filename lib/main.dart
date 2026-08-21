import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

// APK build marker: Cortia-style Android package rebuild 2026-08-22.
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const NexoraDubAiApp());
}

class NexoraDubAiApp extends StatelessWidget {
  const NexoraDubAiApp({super.key});

  @override
  Widget build(BuildContext context) {
    const seed = Color(0xFF7C4DFF);
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Nexora Dub.Ai',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: seed,
          brightness: Brightness.dark,
        ),
        scaffoldBackgroundColor: const Color(0xFF0D0F14),
      ),
      home: const HomeScreen(),
    );
  }
}
