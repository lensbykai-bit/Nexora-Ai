import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import '../widgets/feature_card.dart';
import 'project_workspace_screen.dart';
import 'settings_screen.dart';

class ProjectEntry {
  final String name;
  final int size;
  const ProjectEntry(this.name, this.size);
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int index = 0;
  final List<ProjectEntry> projects = [];

  Future<void> openNewProject({int initialStep = 0}) async {
    try {
      final result = await FilePicker.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['mp4', 'mov', 'mkv', 'webm', 'mp3', 'wav', 'm4a', 'aac'],
      );
      if (!mounted || result == null || result.files.isEmpty) return;
      final file = result.files.first;
      final project = ProjectEntry(file.name, file.size);
      setState(() => projects.insert(0, project));
      await Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ProjectWorkspaceScreen(
            fileName: project.name,
            fileSize: project.size,
            initialStep: initialStep,
          ),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Could not open file picker: $e')),
      );
    }
  }

  void openProject(ProjectEntry project) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => ProjectWorkspaceScreen(fileName: project.name, fileSize: project.size),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Nexora Dub.Ai', style: TextStyle(fontWeight: FontWeight.w800)),
            Text('Mobile v1.1', style: TextStyle(fontSize: 11)),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'New project',
            onPressed: openNewProject,
            icon: const Icon(Icons.add_circle_outline),
          ),
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
        children: [
          DashboardPage(
            onNewProject: () => openNewProject(),
            onTranscribe: () => openNewProject(initialStep: 0),
            onTranslate: () => openNewProject(initialStep: 1),
            onVoiceStudio: () => setState(() => index = 2),
          ),
          ProjectsPage(projects: projects, onNew: openNewProject, onOpen: openProject),
          const VoicesPage(),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (value) => setState(() => index = value),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Home'),
          NavigationDestination(icon: Icon(Icons.folder_outlined), selectedIcon: Icon(Icons.folder), label: 'Projects'),
          NavigationDestination(icon: Icon(Icons.record_voice_over_outlined), selectedIcon: Icon(Icons.record_voice_over), label: 'Voices'),
        ],
      ),
    );
  }
}

class DashboardPage extends StatelessWidget {
  final VoidCallback onNewProject;
  final VoidCallback onTranscribe;
  final VoidCallback onTranslate;
  final VoidCallback onVoiceStudio;

  const DashboardPage({
    super.key,
    required this.onNewProject,
    required this.onTranscribe,
    required this.onTranslate,
    required this.onVoiceStudio,
  });

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(24),
            gradient: const LinearGradient(colors: [Color(0xFF6C4DFF), Color(0xFFE454FF)]),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Dub videos your way', style: TextStyle(fontSize: 26, fontWeight: FontWeight.w900)),
              const SizedBox(height: 8),
              const Text('Import video/audio, prepare text, choose language and voice, then continue to export.'),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: onNewProject,
                icon: const Icon(Icons.add),
                label: const Text('Import Video / Audio'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 18),
        const Text('Tools', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        const SizedBox(height: 12),
        FeatureCard(icon: Icons.video_file_outlined, title: 'New Project', subtitle: 'Choose a video or audio file', onTap: onNewProject),
        const SizedBox(height: 10),
        FeatureCard(icon: Icons.subtitles_outlined, title: 'Transcribe', subtitle: 'Open transcript workspace', onTap: onTranscribe),
        const SizedBox(height: 10),
        FeatureCard(icon: Icons.translate_outlined, title: 'Translate', subtitle: 'Choose media and target language', onTap: onTranslate),
        const SizedBox(height: 10),
        FeatureCard(icon: Icons.graphic_eq_outlined, title: 'Voice Studio', subtitle: 'Choose a voice profile', onTap: onVoiceStudio),
      ],
    );
  }
}

class ProjectsPage extends StatelessWidget {
  final List<ProjectEntry> projects;
  final Future<void> Function({int initialStep}) onNew;
  final void Function(ProjectEntry project) onOpen;

  const ProjectsPage({super.key, required this.projects, required this.onNew, required this.onOpen});

  @override
  Widget build(BuildContext context) {
    if (projects.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.folder_open, size: 58),
              const SizedBox(height: 12),
              const Text('No projects yet', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
              const SizedBox(height: 12),
              FilledButton.icon(onPressed: () => onNew(), icon: const Icon(Icons.add), label: const Text('Create Project')),
            ],
          ),
        ),
      );
    }
    return ListView.separated(
      padding: const EdgeInsets.all(18),
      itemCount: projects.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (_, i) {
        final p = projects[i];
        return Card(
          child: ListTile(
            onTap: () => onOpen(p),
            leading: const CircleAvatar(child: Icon(Icons.movie_outlined)),
            title: Text(p.name, maxLines: 1, overflow: TextOverflow.ellipsis),
            subtitle: Text('${(p.size / (1024 * 1024)).toStringAsFixed(1)} MB'),
            trailing: const Icon(Icons.chevron_right),
          ),
        );
      },
    );
  }
}

class VoicesPage extends StatefulWidget {
  const VoicesPage({super.key});

  @override
  State<VoicesPage> createState() => _VoicesPageState();
}

class _VoicesPageState extends State<VoicesPage> {
  String selected = 'Nexora Natural 1';
  static const voices = ['Nexora Natural 1', 'Nexora Natural 2', 'Warm Narrator', 'Clear Studio'];

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        const Text('Voice Studio', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900)),
        const SizedBox(height: 6),
        const Text('Select a profile. Real audio preview becomes available after an authorized TTS provider is connected.'),
        const SizedBox(height: 14),
        ...voices.map((voice) => Card(
              child: RadioListTile<String>(
                value: voice,
                groupValue: selected,
                title: Text(voice),
                subtitle: Text(voice.contains('Warm') ? 'Warm narration' : 'Natural studio voice'),
                secondary: IconButton(
                  icon: const Icon(Icons.play_circle_outline),
                  onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('$voice selected. TTS preview is not connected yet.')),
                  ),
                ),
                onChanged: (value) => setState(() => selected = value ?? selected),
              ),
            )),
      ],
    );
  }
}
