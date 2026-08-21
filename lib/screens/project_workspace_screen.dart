import 'package:flutter/material.dart';

class ProjectWorkspaceScreen extends StatefulWidget {
  final String fileName;
  final int fileSize;
  final int initialStep;

  const ProjectWorkspaceScreen({
    super.key,
    required this.fileName,
    required this.fileSize,
    this.initialStep = 0,
  });

  @override
  State<ProjectWorkspaceScreen> createState() => _ProjectWorkspaceScreenState();
}

class _ProjectWorkspaceScreenState extends State<ProjectWorkspaceScreen> {
  late int step;
  final transcriptController = TextEditingController();
  String targetLanguage = 'Khmer';
  String voice = 'Nexora Natural 1';

  static const steps = ['Transcribe', 'Translate', 'Voice', 'Export'];
  static const languages = ['Khmer', 'English', 'Chinese', 'French', 'Spanish', 'German', 'Russian'];
  static const voices = ['Nexora Natural 1', 'Nexora Natural 2', 'Warm Narrator', 'Clear Studio'];

  @override
  void initState() {
    super.initState();
    step = widget.initialStep.clamp(0, 3);
  }

  @override
  void dispose() {
    transcriptController.dispose();
    super.dispose();
  }

  void info(String message) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    final mb = widget.fileSize / (1024 * 1024);
    return Scaffold(
      appBar: AppBar(title: const Text('Project Workspace')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          Card(
            child: ListTile(
              leading: const CircleAvatar(child: Icon(Icons.movie_outlined)),
              title: Text(widget.fileName, maxLines: 1, overflow: TextOverflow.ellipsis),
              subtitle: Text('${mb.toStringAsFixed(1)} MB'),
              trailing: const Icon(Icons.check_circle_outline),
            ),
          ),
          const SizedBox(height: 14),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: List.generate(steps.length, (i) {
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ChoiceChip(
                    label: Text('${i + 1}. ${steps[i]}'),
                    selected: step == i,
                    onSelected: (_) => setState(() => step = i),
                  ),
                );
              }),
            ),
          ),
          const SizedBox(height: 18),
          if (step == 0) _transcribePanel(),
          if (step == 1) _translatePanel(),
          if (step == 2) _voicePanel(),
          if (step == 3) _exportPanel(),
        ],
      ),
    );
  }

  Widget _transcribePanel() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Text('Transcript', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        const SizedBox(height: 8),
        const Text('Type or paste a transcript. Automatic speech-to-text can be connected later from an authorized provider.'),
        const SizedBox(height: 14),
        TextField(
          controller: transcriptController,
          minLines: 7,
          maxLines: 12,
          decoration: const InputDecoration(hintText: 'Enter transcript here…', border: OutlineInputBorder()),
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: () {
            info('Transcript saved in this workspace.');
            setState(() => step = 1);
          },
          icon: const Icon(Icons.arrow_forward),
          label: const Text('Save & Continue'),
        ),
      ],
    );
  }

  Widget _translatePanel() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Text('Translation', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: targetLanguage,
          decoration: const InputDecoration(labelText: 'Target language', border: OutlineInputBorder()),
          items: languages.map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
          onChanged: (value) => setState(() => targetLanguage = value ?? targetLanguage),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: transcriptController,
          minLines: 7,
          maxLines: 12,
          decoration: const InputDecoration(labelText: 'Text to translate', border: OutlineInputBorder()),
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: () {
            if (transcriptController.text.trim().isEmpty) {
              info('Add transcript text first.');
              return;
            }
            info('Language selected: $targetLanguage. Connect an authorized translation API to generate translated text.');
          },
          icon: const Icon(Icons.translate),
          label: const Text('Prepare Translation'),
        ),
        TextButton(onPressed: () => setState(() => step = 2), child: const Text('Continue to Voice')),
      ],
    );
  }

  Widget _voicePanel() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Text('Voice Studio', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: voice,
          decoration: const InputDecoration(labelText: 'Voice profile', border: OutlineInputBorder()),
          items: voices.map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
          onChanged: (value) => setState(() => voice = value ?? voice),
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: () => info('$voice selected. Audio preview requires a connected TTS provider.'),
          icon: const Icon(Icons.play_arrow),
          label: const Text('Preview Voice'),
        ),
        FilledButton.icon(
          onPressed: () {
            info('$voice assigned to this project.');
            setState(() => step = 3);
          },
          icon: const Icon(Icons.check),
          label: const Text('Assign & Continue'),
        ),
      ],
    );
  }

  Widget _exportPanel() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const Text('Export', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        const SizedBox(height: 8),
        const Text('Project controls are ready. Final dubbed-video rendering needs an authorized media renderer/TTS backend.'),
        const SizedBox(height: 14),
        ListTile(
          leading: const Icon(Icons.high_quality_outlined),
          title: const Text('Video quality'),
          subtitle: const Text('1080p'),
          trailing: const Icon(Icons.check),
          onTap: () => info('1080p selected.'),
        ),
        ListTile(
          leading: const Icon(Icons.subtitles_outlined),
          title: const Text('Burn subtitles'),
          subtitle: const Text('Enabled'),
          trailing: const Icon(Icons.check),
          onTap: () => info('Subtitle option selected.'),
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: () => info('Export renderer is not connected yet. The button is working correctly.'),
          icon: const Icon(Icons.download_outlined),
          label: const Text('Export Video'),
        ),
      ],
    );
  }
}
