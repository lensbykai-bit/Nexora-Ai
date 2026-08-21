import 'package:flutter/material.dart';
import '../services/app_config.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.all(18),
        children: [
          ListTile(
            leading: const Icon(Icons.badge_outlined),
            title: const Text('App identity'),
            subtitle: Text('${AppConfig.appName} • ${AppConfig.packageName}'),
          ),
          const ListTile(
            leading: Icon(Icons.cloud_off_outlined),
            title: Text('Backend'),
            subtitle: Text('Not connected by default'),
          ),
          const ListTile(
            leading: Icon(Icons.verified_user_outlined),
            title: Text('Privacy'),
            subtitle: Text(
              'No external legacy domain, IP, login or session endpoint is included.',
            ),
          ),
        ],
      ),
    );
  }
}
