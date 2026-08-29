import 'package:flutter/material.dart';

void main() => runApp(const PinkaApp());

class PinkaApp extends StatelessWidget {
  const PinkaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'PINKA Ai',
      theme: ThemeData(
        brightness: Brightness.dark,
        useMaterial3: true,
        scaffoldBackgroundColor: const Color(0xFF12000D),
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFFF3D9A),
          brightness: Brightness.dark,
        ),
      ),
      home: const PinkaShell(),
    );
  }
}

class PinkaShell extends StatefulWidget {
  const PinkaShell({super.key});

  @override
  State<PinkaShell> createState() => _PinkaShellState();
}

class _PinkaShellState extends State<PinkaShell> {
  int index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = <Widget>[
      const HomePage(),
      const SimplePage(title: 'Projects', icon: Icons.folder_copy_rounded, text: 'Your dubbing and voice projects will appear here.'),
      const SimplePage(title: 'Create', icon: Icons.add_circle_outline_rounded, text: 'Choose AI Dubbing, Voice Generator, or Subtitle Translate to start a new project.'),
      const SimplePage(title: 'Voices', icon: Icons.graphic_eq_rounded, text: 'Manage character voices and your preferred AI voice presets.'),
      const SimplePage(title: 'Profile', icon: Icons.person_outline_rounded, text: 'PINKA Ai settings, language, theme, and account preferences.'),
    ];

    return Scaffold(
      extendBody: true,
      body: IndexedStack(index: index, children: pages),
      bottomNavigationBar: PinkaBottomNav(
        currentIndex: index,
        onChanged: (value) => setState(() => index = value),
      ),
    );
  }
}

class PinkBackground extends StatelessWidget {
  final Widget child;
  const PinkBackground({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF30001F), Color(0xFF10000C), Color(0xFF220015)],
        ),
      ),
      child: Stack(
        children: [
          Positioned(top: -100, left: -90, child: _glow(280, const Color(0x55FF2F98))),
          Positioned(top: 260, right: -120, child: _glow(300, const Color(0x44FF72C1))),
          Positioned(bottom: 20, left: -110, child: _glow(260, const Color(0x33C31874))),
          Positioned.fill(child: child),
        ],
      ),
    );
  }

  Widget _glow(double size, Color color) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        boxShadow: [BoxShadow(color: color, blurRadius: 95, spreadRadius: 35)],
      ),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  void openFeature(BuildContext context, String title, IconData icon, String text) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => FeaturePage(title: title, icon: icon, text: text),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return PinkBackground(
      child: SafeArea(
        bottom: false,
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(18, 14, 18, 118),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Header(),
              const SizedBox(height: 22),
              const HeroCard(),
              const SizedBox(height: 22),
              Row(
                children: [
                  Expanded(
                    child: FeatureCard(
                      icon: Icons.mic_rounded,
                      title: 'AI Dubbing',
                      subtitle: 'Dub videos with AI perfection',
                      onTap: () => openFeature(context, 'AI Dubbing', Icons.mic_rounded, 'Upload a video, prepare subtitles, and generate synchronized AI voices.'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FeatureCard(
                      icon: Icons.graphic_eq_rounded,
                      title: 'Voice Generator',
                      subtitle: 'Create realistic AI voices',
                      onTap: () => openFeature(context, 'Voice Generator', Icons.graphic_eq_rounded, 'Turn your script into expressive voice using your connected AI voice service.'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              WideFeatureCard(
                icon: Icons.translate_rounded,
                title: 'Translate Subtitle',
                subtitle: 'Translate and sync subtitles instantly',
                onTap: () => openFeature(context, 'Translate Subtitle', Icons.translate_rounded, 'Import SRT or subtitle text, choose a target language, and prepare translated subtitles.'),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: FeatureCard(
                      icon: Icons.record_voice_over_rounded,
                      title: 'Character Voices',
                      subtitle: 'Voices for every character',
                      onTap: () => openFeature(context, 'Character Voices', Icons.record_voice_over_rounded, 'Assign a different voice profile to every speaker or character.'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FeatureCard(
                      icon: Icons.ios_share_rounded,
                      title: 'Export',
                      subtitle: 'Export in high quality',
                      onTap: () => openFeature(context, 'Export', Icons.ios_share_rounded, 'Prepare the completed project for export.'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 26),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Recent Projects', style: TextStyle(fontSize: 21, fontWeight: FontWeight.w900)),
                  TextButton(onPressed: () {}, child: const Text('View All  ›', style: TextStyle(color: Color(0xFFFF9ED0)))),
                ],
              ),
              const ProjectTile(),
            ],
          ),
        ),
      ),
    );
  }
}

class Header extends StatelessWidget {
  const Header({super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 70,
          height: 70,
          padding: const EdgeInsets.all(3),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: const Color(0xFFFF70BC), width: 1.4),
            boxShadow: const [BoxShadow(color: Color(0x77FF2D97), blurRadius: 22, spreadRadius: 2)],
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(19),
            child: Image.asset('assets/images/pinka_logo.png', fit: BoxFit.cover),
          ),
        ),
        const SizedBox(width: 13),
        const Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              GradientText('PINKA Ai', size: 33),
              SizedBox(height: 2),
              Text('AI DUBBING  |  VOICE GENERATOR  |  5D', style: TextStyle(fontSize: 9.2, letterSpacing: .8, color: Color(0xFFFFC8E4))),
            ],
          ),
        ),
        const GlowCircle(icon: Icons.workspace_premium_rounded, size: 46),
      ],
    );
  }
}

class GradientText extends StatelessWidget {
  final String text;
  final double size;
  const GradientText(this.text, {super.key, required this.size});

  @override
  Widget build(BuildContext context) {
    return ShaderMask(
      shaderCallback: (rect) => const LinearGradient(
        colors: [Color(0xFFFFF1F9), Color(0xFFFF68B3), Color(0xFFFFF8FC)],
      ).createShader(rect),
      child: Text(
        text,
        style: TextStyle(
          fontSize: size,
          height: 1,
          fontWeight: FontWeight.w900,
          color: Colors.white,
          shadows: const [Shadow(color: Color(0xFFFF2E97), blurRadius: 15)],
        ),
      ),
    );
  }
}

class HeroCard extends StatelessWidget {
  const HeroCard({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 228,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(30),
        gradient: const LinearGradient(colors: [Color(0xFF81174F), Color(0xFF350624), Color(0xFF741149)]),
        border: Border.all(color: const Color(0xFFFF6DBB), width: 1.4),
        boxShadow: const [BoxShadow(color: Color(0x66FF2E98), blurRadius: 30, offset: Offset(0, 12))],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(29),
        child: Stack(
          children: [
            Positioned(right: -18, top: 7, bottom: 7, width: 205, child: Image.asset('assets/images/pinka_logo.png', fit: BoxFit.cover)),
            Padding(
              padding: const EdgeInsets.all(25),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Bring Every\nStory to Life', style: TextStyle(fontSize: 28, height: 1.05, fontWeight: FontWeight.w900, shadows: [Shadow(color: Color(0xFFFF5EAE), blurRadius: 13)])),
                  const SizedBox(height: 10),
                  const Text('AI Dubbing & Voice\nMagic in 5D', style: TextStyle(fontSize: 15.5, height: 1.35, color: Color(0xFFFFD3E8))),
                  const Spacer(),
                  FilledButton.icon(
                    onPressed: () {},
                    icon: const Icon(Icons.auto_awesome, size: 16),
                    label: const Text('Create Now'),
                    style: FilledButton.styleFrom(
                      backgroundColor: const Color(0xFFFF4DA5),
                      padding: const EdgeInsets.symmetric(horizontal: 19, vertical: 13),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(22)),
                      shadowColor: const Color(0xFFFF44A1),
                      elevation: 10,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class FeatureCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  const FeatureCard({super.key, required this.icon, required this.title, required this.subtitle, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      onTap: onTap,
      padding: const EdgeInsets.all(15),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          FeatureIcon(icon),
          const SizedBox(height: 13),
          Row(children: [Expanded(child: Text(title, style: const TextStyle(fontSize: 16.5, fontWeight: FontWeight.w900))), const Icon(Icons.chevron_right_rounded, color: Color(0xFFFFB0D6))]),
          const SizedBox(height: 5),
          Text(subtitle, style: const TextStyle(fontSize: 11.5, height: 1.35, color: Color(0xFFFFC5E0))),
        ],
      ),
    );
  }
}

class WideFeatureCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  const WideFeatureCard({super.key, required this.icon, required this.title, required this.subtitle, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      onTap: onTap,
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          FeatureIcon(icon),
          const SizedBox(width: 15),
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(title, style: const TextStyle(fontSize: 17.5, fontWeight: FontWeight.w900)), const SizedBox(height: 4), Text(subtitle, style: const TextStyle(fontSize: 12.2, color: Color(0xFFFFC5E0)))])),
          const Icon(Icons.chevron_right_rounded, color: Color(0xFFFFB0D6)),
        ],
      ),
    );
  }
}

class GlassCard extends StatelessWidget {
  final Widget child;
  final EdgeInsets padding;
  final VoidCallback? onTap;
  const GlassCard({super.key, required this.child, required this.padding, this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Container(
        padding: padding,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          gradient: const LinearGradient(begin: Alignment.topLeft, end: Alignment.bottomRight, colors: [Color(0xFF711646), Color(0xFF3C0A2B), Color(0xFF4C0A33)]),
          border: Border.all(color: const Color(0xFFFF58AC), width: 1.15),
          boxShadow: const [BoxShadow(color: Color(0x44FF2E98), blurRadius: 18, offset: Offset(0, 8))],
        ),
        child: child,
      ),
    );
  }
}

class FeatureIcon extends StatelessWidget {
  final IconData icon;
  const FeatureIcon(this.icon, {super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 58,
      height: 58,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: const LinearGradient(colors: [Color(0xFFFF6CB7), Color(0xFFB31267)]),
        border: Border.all(color: const Color(0xFFFFB8DB)),
        boxShadow: const [BoxShadow(color: Color(0x99FF3F9F), blurRadius: 18)],
      ),
      child: Icon(icon, size: 29, color: Colors.white),
    );
  }
}

class GlowCircle extends StatelessWidget {
  final IconData icon;
  final double size;
  const GlowCircle({super.key, required this.icon, required this.size});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: const Color(0x33260018),
        border: Border.all(color: const Color(0xFFFF5EAF)),
        boxShadow: const [BoxShadow(color: Color(0x77FF3B9E), blurRadius: 18)],
      ),
      child: Icon(icon, color: const Color(0xFFFFE5F2)),
    );
  }
}

class ProjectTile extends StatelessWidget {
  const ProjectTile({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      padding: const EdgeInsets.all(12),
      child: Row(
        children: [
          Container(
            width: 86,
            height: 60,
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(14), gradient: const LinearGradient(colors: [Color(0xFFFF5CAB), Color(0xFF4C0A35)])),
            child: const Icon(Icons.play_circle_fill_rounded, size: 40, color: Colors.white),
          ),
          const SizedBox(width: 13),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('My Dubbing Project.mp4', maxLines: 1, overflow: TextOverflow.ellipsis, style: TextStyle(fontSize: 14.5, fontWeight: FontWeight.w800)),
                SizedBox(height: 7),
                Text('Today, 09:20 PM   •   2:45', style: TextStyle(fontSize: 10.5, color: Color(0xFFFFB8D6))),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(20), border: Border.all(color: const Color(0xFFFF5AAE))),
            child: const Text('Completed', style: TextStyle(fontSize: 9.5, color: Color(0xFFFFB7D8))),
          ),
        ],
      ),
    );
  }
}

class PinkaBottomNav extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onChanged;
  const PinkaBottomNav({super.key, required this.currentIndex, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    const icons = [Icons.home_rounded, Icons.folder_copy_rounded, Icons.add_rounded, Icons.graphic_eq_rounded, Icons.person_outline_rounded];
    const labels = ['Home', 'Projects', 'Create', 'Voices', 'Profile'];

    return SafeArea(
      minimum: const EdgeInsets.fromLTRB(16, 0, 16, 10),
      child: Container(
        height: 76,
        padding: const EdgeInsets.symmetric(horizontal: 6),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(30),
          gradient: const LinearGradient(colors: [Color(0xFF5A103B), Color(0xFF29071E)]),
          border: Border.all(color: const Color(0xFFFF62B0)),
          boxShadow: const [BoxShadow(color: Color(0x99FF2F98), blurRadius: 26)],
        ),
        child: Row(
          children: List.generate(5, (i) {
            if (i == 2) {
              return Expanded(
                child: GestureDetector(
                  onTap: () => onChanged(i),
                  child: Transform.translate(
                    offset: const Offset(0, -11),
                    child: Container(
                      width: 67,
                      height: 67,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: const LinearGradient(colors: [Color(0xFFFF7ABC), Color(0xFFFF2791)]),
                        border: Border.all(color: const Color(0xFFFFC7E3), width: 2),
                        boxShadow: const [BoxShadow(color: Color(0xAAFF3A9F), blurRadius: 22, spreadRadius: 2)],
                      ),
                      child: const Icon(Icons.add_rounded, size: 40, color: Colors.white),
                    ),
                  ),
                ),
              );
            }
            final selected = currentIndex == i;
            return Expanded(
              child: InkWell(
                onTap: () => onChanged(i),
                borderRadius: BorderRadius.circular(18),
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(icons[i], color: selected ? Colors.white : const Color(0xFFDF92B9), shadows: selected ? const [Shadow(color: Color(0xFFFF3E9F), blurRadius: 15)] : null),
                      const SizedBox(height: 3),
                      Text(labels[i], style: TextStyle(fontSize: 10.2, fontWeight: selected ? FontWeight.w800 : FontWeight.w500, color: selected ? Colors.white : const Color(0xFFDF92B9))),
                    ],
                  ),
                ),
              ),
            );
          }),
        ),
      ),
    );
  }
}

class FeaturePage extends StatelessWidget {
  final String title;
  final IconData icon;
  final String text;
  const FeaturePage({super.key, required this.title, required this.icon, required this.text});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: PinkBackground(
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    IconButton.filledTonal(onPressed: () => Navigator.pop(context), icon: const Icon(Icons.arrow_back_rounded)),
                    const SizedBox(width: 12),
                    Expanded(child: GradientText(title, size: 27)),
                  ],
                ),
                const SizedBox(height: 30),
                Center(child: FeatureIcon(icon)),
                const SizedBox(height: 26),
                Text(text, style: const TextStyle(fontSize: 16, height: 1.55, color: Color(0xFFFFD0E6))),
                const SizedBox(height: 24),
                const GlassCard(
                  padding: EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('PINKA Ai', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900)),
                      SizedBox(height: 8),
                      Text('This build contains the redesigned Pink 5D interface. Connect your own AI processing service to activate this workflow.', style: TextStyle(height: 1.45, color: Color(0xFFFFC1DF))),
                    ],
                  ),
                ),
                const Spacer(),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: () {},
                    icon: const Icon(Icons.auto_awesome),
                    label: const Padding(padding: EdgeInsets.symmetric(vertical: 14), child: Text('Start', style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16))),
                    style: FilledButton.styleFrom(backgroundColor: const Color(0xFFFF3F9F), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24))),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class SimplePage extends StatelessWidget {
  final String title;
  final IconData icon;
  final String text;
  const SimplePage({super.key, required this.title, required this.icon, required this.text});

  @override
  Widget build(BuildContext context) {
    return PinkBackground(
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 24, 20, 116),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              GradientText(title, size: 31),
              const SizedBox(height: 28),
              GlassCard(
                padding: const EdgeInsets.all(24),
                child: Column(
                  children: [
                    FeatureIcon(icon),
                    const SizedBox(height: 20),
                    Text(text, textAlign: TextAlign.center, style: const TextStyle(fontSize: 15.5, height: 1.5, color: Color(0xFFFFC8E1))),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
