import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../features/home/home_screen.dart';
import '../../features/quran/surah_list_screen.dart';
import '../../features/sunnah/sunnah_home_screen.dart';
import '../../features/library/library_screen.dart';
import '../../features/settings/settings_screen.dart';
import '../../features/search/global_search_screen.dart';

/// Adaptive root: bottom NavigationBar on phones, NavigationRail on
/// tablets/large screens. Respects RTL + iOS safe areas + back gesture
/// (no custom back handling — platform default preserved).
class AdaptiveScaffold extends ConsumerStatefulWidget {
  const AdaptiveScaffold({super.key});

  @override
  ConsumerState<AdaptiveScaffold> createState() => _AdaptiveScaffoldState();
}

class _AdaptiveScaffoldState extends ConsumerState<AdaptiveScaffold> {
  int _index = 0;

  static const _pages = [
    HomeScreen(),
    SurahListScreen(),
    SunnahHomeScreen(),
    LibraryScreen(),
    SettingsScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final width = MediaQuery.sizeOf(context).width;
    final isLarge = width >= 840;

    final destinations = [
      NavigationDestination(
          icon: const Icon(Icons.home_outlined),
          selectedIcon: const Icon(Icons.home),
          label: s.t('home')),
      NavigationDestination(
          icon: const Icon(Icons.menu_book_outlined),
          selectedIcon: const Icon(Icons.menu_book),
          label: s.t('quran')),
      NavigationDestination(
          icon: const Icon(Icons.auto_stories_outlined),
          selectedIcon: const Icon(Icons.auto_stories),
          label: s.t('sunnah')),
      NavigationDestination(
          icon: const Icon(Icons.bookmark_outline),
          selectedIcon: const Icon(Icons.bookmark),
          label: s.t('library')),
      NavigationDestination(
          icon: const Icon(Icons.settings_outlined),
          selectedIcon: const Icon(Icons.settings),
          label: s.t('settings')),
    ];

    if (isLarge) {
      return Scaffold(
        body: SafeArea(
          child: Row(
            children: [
              NavigationRail(
                selectedIndex: _index,
                onDestinationSelected: (i) =>
                    setState(() => _index = i),
                labelType: NavigationRailLabelType.all,
                destinations: destinations
                    .map((d) => NavigationRailDestination(
                        icon: d.icon,
                        selectedIcon: d.selectedIcon,
                        label: Text(d.label)))
                    .toList(),
                trailing: IconButton(
                  tooltip: s.t('search'),
                  icon: const Icon(Icons.search),
                  onPressed: () => Navigator.of(context).push(
                    MaterialPageRoute(
                        builder: (_) => const GlobalSearchScreen()),
                  ),
                ),
              ),
              const VerticalDivider(width: 1),
              Expanded(child: _pages[_index]),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      body: SafeArea(child: _pages[_index]),
      bottomNavigationBar: NavigationBar(
        labelBehavior: MediaQuery.textScalerOf(context).scale(1) >= 1.5
            ? NavigationDestinationLabelBehavior.onlyShowSelected
            : NavigationDestinationLabelBehavior.alwaysShow,
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: destinations,
      ),
      floatingActionButton: _index != 0
          ? FloatingActionButton(
              tooltip: s.t('search'),
              onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) => const GlobalSearchScreen()),
              ),
              child: const Icon(Icons.search),
            )
          : null,
    );
  }
}
