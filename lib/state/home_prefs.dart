import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Home dashboard layout: order + visibility, persisted. Users can
/// hide or reorder sections without touching code.
enum HomeSection {
  continueReading,
  quickActions,
  dailyAyah,
  dailyHadith,
  bookmarks,
  recent,
  stats,
}

extension HomeSectionX on HomeSection {
  String get storageKey => name;

  String label(String locale) {
    const labels = {
      'continueReading': {
        'ar': 'مواصلة القراءة',
        'en': 'Continue reading',
        'fr': 'Continuer la lecture'
      },
      'quickActions': {
        'ar': 'وصول سريع',
        'en': 'Quick actions',
        'fr': 'Accès rapide'
      },
      'dailyAyah': {
        'ar': 'آية اليوم',
        'en': 'Daily ayah',
        'fr': 'Verset du jour'
      },
      'dailyHadith': {
        'ar': 'حديث اليوم',
        'en': 'Daily hadith',
        'fr': 'Hadith du jour'
      },
      'bookmarks': {
        'ar': 'العلامات',
        'en': 'Bookmarks',
        'fr': 'Favoris'
      },
      'recent': {
        'ar': 'شوهد مؤخرًا',
        'en': 'Recently viewed',
        'fr': 'Récemment consultés'
      },
      'stats': {'ar': 'مكتبتك', 'en': 'Your library', 'fr': 'Votre bibliothèque'},
    };
    return labels[storageKey]?[locale] ?? labels[storageKey]?['en'] ?? name;
  }
}

class HomeLayout {
  const HomeLayout({required this.order, required this.hidden});

  final List<HomeSection> order;
  final Set<HomeSection> hidden;

  List<HomeSection> get visible =>
      order.where((s) => !hidden.contains(s)).toList();
}

class HomeLayoutNotifier extends Notifier<HomeLayout> {
  @override
  HomeLayout build() {
    final initial =
        const HomeLayout(order: HomeSection.values, hidden: {});
    Future.microtask(() => _load(initial));
    return initial;
  }

  Future<void> _load(HomeLayout initial) async {
    final p = await SharedPreferences.getInstance();
    if (!ref.mounted) return;
    // Don't clobber changes made while loading.
    if (!identical(state, initial)) {
      return;
    }
    final raw = p.getStringList('home.order');
    final hiddenRaw = p.getStringList('home.hidden') ?? [];
    if (raw != null && raw.isNotEmpty) {
      final order = <HomeSection>[];
      for (final k in raw) {
        for (final s in HomeSection.values) {
          if (s.storageKey == k && !order.contains(s)) {
            order.add(s);
          }
        }
      }
      // Append any new sections added in updates.
      for (final s in HomeSection.values) {
        if (!order.contains(s)) {
          order.add(s);
        }
      }
      final hidden = HomeSection.values
          .where((s) => hiddenRaw.contains(s.storageKey))
          .toSet();
      state = HomeLayout(order: order, hidden: hidden);
    }
  }

  Future<void> _save() async {
    final p = await SharedPreferences.getInstance();
    await p.setStringList(
        'home.order', state.order.map((s) => s.storageKey).toList());
    await p.setStringList(
        'home.hidden', state.hidden.map((s) => s.storageKey).toList());
  }

  /// [newIndex] is already adjusted for the removed item
  /// (onReorderItem contract).
  Future<void> reorder(int oldIndex, int newIndex) async {
    final order = [...state.order];
    final item = order.removeAt(oldIndex);
    order.insert(newIndex.clamp(0, order.length), item);
    state = HomeLayout(order: order, hidden: state.hidden);
    await _save();
  }

  Future<void> toggleHidden(HomeSection s) async {
    final hidden = {...state.hidden};
    if (!hidden.remove(s)) {
      hidden.add(s);
    }
    state = HomeLayout(order: state.order, hidden: hidden);
    await _save();
  }
}

final homeLayoutProvider =
    NotifierProvider<HomeLayoutNotifier, HomeLayout>(
        HomeLayoutNotifier.new);
