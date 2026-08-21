import 'package:flutter/material.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String preset = 'YouTube';
  String targetLanguage = 'KH';
  String translator = 'Gemini';
  bool subtitle = true;
  bool gemini = true;
  bool google = false;
  bool voice = true;
  bool effect = true;

  void _toast(String text) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(text)));
  }

  Widget _section({required Widget child, Color? borderColor}) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF1A2435),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: borderColor ?? const Color(0xFF2E3C52), width: 1.3),
        boxShadow: const [BoxShadow(blurRadius: 16, color: Color(0x33000000), offset: Offset(0, 8))],
      ),
      child: child,
    );
  }

  Widget _button({
    required String label,
    required IconData icon,
    required VoidCallback onPressed,
    Color background = const Color(0xFF2F3C50),
    Color foreground = Colors.white,
    double height = 58,
  }) {
    return SizedBox(
      height: height,
      child: FilledButton.icon(
        style: FilledButton.styleFrom(
          backgroundColor: background,
          foregroundColor: foreground,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        ),
        onPressed: onPressed,
        icon: Icon(icon, size: 21),
        label: Text(label, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.w800)),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0C1422),
      appBar: AppBar(
        backgroundColor: const Color(0xFF111A2C),
        foregroundColor: Colors.white,
        elevation: 0,
        title: const Text('Nexora Dub.Ai Plus', style: TextStyle(fontWeight: FontWeight.w900, fontSize: 22)),
        actions: [
          const Center(child: Padding(padding: EdgeInsets.only(right: 8), child: Text('🔑 3 Keys', style: TextStyle(color: Color(0xFFFFD84D), fontWeight: FontWeight.w800)))),
          IconButton(onPressed: () => _toast('Exit button ready'), icon: const Icon(Icons.logout_rounded)),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(14, 14, 14, 26),
          children: [
            _section(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Text('⭐ Preset / សំឡេង', style: TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
                      const Spacer(),
                      OutlinedButton.icon(
                        onPressed: () => _toast('Preset history'),
                        icon: const Icon(Icons.history, size: 18),
                        label: const Text('History'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  Row(
                    children: [
                      Expanded(child: _presetDrop()),
                      const SizedBox(width: 8),
                      Expanded(child: _langDrop()),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(child: _button(label: 'Preview Voice', icon: Icons.volume_up_rounded, onPressed: () => _toast('Voice preview'))),
                      const SizedBox(width: 8),
                      Expanded(child: _button(label: 'Upload', icon: Icons.cloud_upload_rounded, background: const Color(0xFFFF5964), onPressed: () => _toast('Upload picker will open in full engine build'))),
                    ],
                  ),
                ],
              ),
            ),
            _section(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('📁 បញ្ចូលឯកសារ SRT Subtitles', style: TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
                  const SizedBox(height: 14),
                  SizedBox(
                    width: double.infinity,
                    child: _button(label: 'Import SRT Subtitle', icon: Icons.note_add_rounded, height: 68, background: const Color(0xFF2855D9), onPressed: () => _toast('SRT import selected')),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(child: _button(label: 'Gemini', icon: Icons.auto_awesome_rounded, background: const Color(0xFF8539DB), onPressed: () => setState(() => translator = 'Gemini'))),
                      const SizedBox(width: 8),
                      Expanded(child: _button(label: 'Google', icon: Icons.translate_rounded, background: const Color(0xFF0D9CCB), onPressed: () => setState(() => translator = 'Google'))),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text('Translator: $translator', style: const TextStyle(color: Color(0xFFAAB8CC), fontWeight: FontWeight.w700)),
                ],
              ),
            ),
            _section(
              borderColor: const Color(0xFF087D87),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Text('🎬 AI Studio Suite', style: TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
                      const Spacer(),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
                        decoration: BoxDecoration(borderRadius: BorderRadius.circular(14), border: Border.all(color: const Color(0xFFB834D7))),
                        child: const Text('📱 MOBILE SUITE', style: TextStyle(color: Color(0xFFE24EF6), fontWeight: FontWeight.w900)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(flex: 6, child: _button(label: 'Browse File', icon: Icons.file_open_rounded, background: const Color(0xFF12A9C8), height: 64, onPressed: () => _toast('Browse video/audio'))),
                      const SizedBox(width: 8),
                      Expanded(flex: 5, child: _button(label: 'API Keys (2/3)', icon: Icons.key_rounded, height: 64, onPressed: () => _toast('API key manager'))),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(child: _button(label: 'Export', icon: Icons.movie_rounded, background: const Color(0xFF12A779), height: 52, onPressed: () => _toast('Export'))),
                      const SizedBox(width: 5),
                      Expanded(child: _button(label: 'Split', icon: Icons.content_cut_rounded, background: const Color(0xFF9C4E00), height: 52, onPressed: () => _toast('Split audio'))),
                      const SizedBox(width: 5),
                      Expanded(child: _button(label: 'STT Auto', icon: Icons.mic_rounded, background: const Color(0xFF17485A), height: 52, onPressed: () => _toast('Automatic STT'))),
                      const SizedBox(width: 5),
                      Expanded(child: _button(label: 'Vocal', icon: Icons.graphic_eq_rounded, background: const Color(0xFF6D1FB2), height: 52, onPressed: () => _toast('Vocal tool'))),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(color: const Color(0xFF101D2E), borderRadius: BorderRadius.circular(18), border: Border.all(color: const Color(0xFF0C6C77))),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('⚙️ 1-Click Auto Export', style: TextStyle(fontWeight: FontWeight.w900)),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 6,
                          runSpacing: 6,
                          children: [
                            _chip('Subtitle', subtitle, (v) => setState(() => subtitle = v)),
                            _chip('Gemini', gemini, (v) => setState(() => gemini = v)),
                            _chip('Google', google, (v) => setState(() => google = v)),
                            _chip('Voice', voice, (v) => setState(() => voice = v)),
                            _chip('Effect', effect, (v) => setState(() => effect = v)),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    child: _button(label: '🎵 Export 1 Click', icon: Icons.auto_fix_high_rounded, background: const Color(0xFF0AB079), height: 68, onPressed: () => _toast('1-Click Export started')),
                  ),
                ],
              ),
            ),
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 8),
              child: Text('Nexora independent mobile UI • AI services are connected only when you add your own authorized API keys.', textAlign: TextAlign.center, style: TextStyle(color: Color(0xFF7F8FA8), fontSize: 12)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _presetDrop() {
    return DropdownButtonFormField<String>(
      initialValue: preset,
      decoration: const InputDecoration(filled: true, fillColor: Color(0xFF2D394C), border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(14)), borderSide: BorderSide.none)),
      items: const ['YouTube', 'TikTok', 'Facebook', 'Movie'].map((v) => DropdownMenuItem(value: v, child: Text(v))).toList(),
      onChanged: (v) => setState(() => preset = v ?? preset),
    );
  }

  Widget _langDrop() {
    return DropdownButtonFormField<String>(
      initialValue: targetLanguage,
      decoration: const InputDecoration(filled: true, fillColor: Color(0xFF2D394C), border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(14)), borderSide: BorderSide.none)),
      items: const ['KH', 'EN', 'CN', 'FR', 'ES', 'DE', 'RU'].map((v) => DropdownMenuItem(value: v, child: Text(v))).toList(),
      onChanged: (v) => setState(() => targetLanguage = v ?? targetLanguage),
    );
  }

  Widget _chip(String label, bool value, ValueChanged<bool> onChanged) {
    return FilterChip(
      selected: value,
      onSelected: onChanged,
      avatar: Icon(value ? Icons.check_box_rounded : Icons.check_box_outline_blank_rounded, size: 18),
      label: Text(label),
      selectedColor: const Color(0xFF0B6770),
      side: BorderSide(color: value ? const Color(0xFF15E5E8) : const Color(0xFF566377)),
      labelStyle: TextStyle(color: value ? const Color(0xFF54F5F4) : const Color(0xFF9AA6B8), fontWeight: FontWeight.w800),
    );
  }
}
