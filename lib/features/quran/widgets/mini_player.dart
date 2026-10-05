import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../data/services/audio_service.dart';
import '../audio_player_screen.dart';

/// Persistent bottom mini-player: shows when audio is playing/loading.
/// Keeps playback visible after leaving AudioPlayerScreen.
class MiniPlayer extends ConsumerWidget {
  const MiniPlayer({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final audio = ref.watch(audioServiceProvider);
    final svc = ref.read(audioServiceProvider.notifier);
    if (!audio.playing && !audio.loading && audio.refKey == null) {
      return const SizedBox.shrink();
    }
    final scheme = Theme.of(context).colorScheme;
    return Container(
      decoration: BoxDecoration(
        color: scheme.surfaceContainerHigh,
        border: Border(top: BorderSide(color: scheme.outlineVariant)),
      ),
      child: SafeArea(
        top: false,
        child: ListTile(
          dense: true,
          leading: audio.loading
              ? const SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : IconButton(
                  icon: Icon(
                      audio.playing ? Icons.pause : Icons.play_arrow),
                  onPressed: () =>
                      audio.playing ? svc.pause() : svc.resume(),
                ),
          title: Text(
            audio.refKey == null
                ? 'Quran Audio'
                : 'Ayah ${audio.refKey}',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          subtitle: audio.error != null
              ? Text(audio.error!,
                  maxLines: 1, overflow: TextOverflow.ellipsis)
              : null,
          trailing: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                icon: const Icon(Icons.stop_outlined),
                onPressed: () => svc.stop(),
              ),
              IconButton(
                icon: const Icon(Icons.open_in_full),
                onPressed: () => Navigator.of(context).push(
                  MaterialPageRoute(
                      builder: (_) => const AudioPlayerScreen()),
                ),
              ),
            ],
          ),
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute(
                builder: (_) => const AudioPlayerScreen()),
          ),
        ),
      ),
    );
  }
}
