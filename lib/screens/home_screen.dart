import 'package:flutter/material.dart';
import '../widgets/feature_card.dart';
import 'settings_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int index = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Nexora Dub.Ai', style: TextStyle(fontWeight: FontWeight.w800)),
            Text('Independent Clean Build', style: TextStyle(fontSize: 11)),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'Settings',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const SettingsScreen()),
            ),
            icon: const Icon(Icons.settings_outlined),
          ),
        ],
      ),
      body: IndexedStack(
        index: index,
        children: const [DashboardPage(), ProjectsPage(), VoicesPage()],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (value) => setState(() => index = value),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: 'Home',
          ),
          NavigationDestination(
            icon: Icon(Icons.folder_outlined),
            selectedIcon: Icon(Icons.folder),
            label: 'Projects',
          ),
          NavigationDestination(
            icon: Icon(Icons.record_voice_over_outlined),
            selectedIcon: Icon(Icons.record_voice_over),
            label: 'Voices',
          ),
        ],
      ),
    );
  }
}

class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(24),
            gradient: const LinearGradient(
              colors: [Color(0xFF6C4DFF), Color(0xFFE454FF)],
            ),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Dub videos your way',
                style: TextStyle(fontSize: 26, fontWeight: FontWeight.w900),
              ),
              SizedBox(height: 8),
              Text(
                'A fresh independent workspace for transcription, translation, voice assignment and export.',
              ),
            ],
          ),
        ),
        const SizedBox(height: 18),
        const Text('Tools', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        const SizedBox(height: 12),
        const FeatureCard(
          icon: Icons.video_file_outlined,
          title: 'New Project',
          subtitle: 'Import a video or audio file',
        ),
        const SizedBox(height: 10),
        const FeatureCard(
          icon: Icons.subtitles_outlined,
          title: 'Transcribe',
          subtitle: 'Prepare speech-to-text workflow',
        ),
        const SizedBox(height: 10),
        const FeatureCard(
          icon: Icons.translate_outlined,
          title: 'Translate',
          subtitle: 'Language workflow placeholder',
        ),
        const SizedBox(height: 10),
        const FeatureCard(
          icon: Icons.graphic_eq_outlined,
          title: 'Voice Studio',
          subtitle: 'Assign and preview voices',
        ),
      ],
    );
  }
}

class ProjectsPage extends StatelessWidget {
  const ProjectsPage({super.key});

  @override
  Widget build(BuildContext context) => const Center(child: Text('No projects yet'));
}

class VoicesPage extends StatelessWidget {
  const VoicesPage({super.key});

  @override
  Widget build(BuildContext context) =>
      const Center(child: Text('Voice providers can be connected here'));
}
