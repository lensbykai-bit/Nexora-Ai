import 'package:flutter/material.dart';
import '../services/app_config.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool confirmBeforeExport = true;
  bool subtitlesByDefault = true;

  void showInfo(String title, String message) {
    showDialog<void>(
      context: context,
      builder: (_) => AlertDialog(
        title: Text(title),
        content: Text(message),
        actions: [TextButton(onPressed: () => Navigator.pop(context), child: const Text('OK'))],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          ListTile(
            onTap: () => showInfo('App identity', '${AppConfig.appName}\n${AppConfig.packageName}\nVersion 1.1.0'),
            leading: const Icon(Icons.badge_outlined),
            title: const Text('App identity'),
            subtitle: Text('${AppConfig.appName} • ${AppConfig.packageName}'),
            trailing: const Icon(Icons.chevron_right),
          ),
          ListTile(
            onTap: () => showInfo('Backend', 'No speech, translation, TTS or render server is connected yet. Add only services you own or are authorized to use.'),
            leading: const Icon(Icons.cloud_off_outlined),
            title: const Text('Backend / API Keys'),
            subtitle: const Text('Not connected'),
            trailing: const Icon(Icons.chevron_right),
          ),
          SwitchListTile(
            value: subtitlesByDefault,
            onChanged: (value) => setState(() => subtitlesByDefault = value),
            secondary: const Icon(Icons.subtitles_outlined),
            title: const Text('Subtitles by default'),
          ),
          SwitchListTile(
            value: confirmBeforeExport,
            onChanged: (value) => setState(() => confirmBeforeExport = value),
            secondary: const Icon(Icons.verified_outlined),
            title: const Text('Confirm before export'),
          ),
          ListTile(
            onTap: () => showInfo('Privacy', 'This independent app has no legacy login/session server configured. External services remain disconnected until you add an authorized provider.'),
            leading: const Icon(Icons.verified_user_outlined),
            title: const Text('Privacy'),
            subtitle: const Text('Independent configuration'),
            trailing: const Icon(Icons.chevron_right),
          ),
        ],
      ),
    );
  }
}
