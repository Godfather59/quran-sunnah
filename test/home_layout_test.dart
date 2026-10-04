import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/state/home_prefs.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('home layout: defaults, hide, reorder persist', () async {
    SharedPreferences.setMockInitialValues({});
    final container = ProviderContainer();
    addTearDown(container.dispose);

    // Defaults: all sections visible, canonical order.
    await Future<void>.delayed(const Duration(milliseconds: 100));
    var layout = container.read(homeLayoutProvider);
    expect(layout.visible.length, HomeSection.values.length);

    // Hide a section.
    await container
        .read(homeLayoutProvider.notifier)
        .toggleHidden(HomeSection.stats);
    layout = container.read(homeLayoutProvider);
    expect(layout.hidden, contains(HomeSection.stats));
    expect(layout.visible.length, HomeSection.values.length - 1);

    // Reorder: move first to last (onReorderItem-adjusted index).
    final first = layout.order.first;
    await container
        .read(homeLayoutProvider.notifier)
        .reorder(0, layout.order.length - 1);
    layout = container.read(homeLayoutProvider);
    expect(layout.order.last, first);

    // New container reads persisted state.
    final container2 = ProviderContainer();
    addTearDown(container2.dispose);
    HomeLayout layout2 = container2.read(homeLayoutProvider);
    for (var i = 0;
        i < 50 && !layout2.hidden.contains(HomeSection.stats);
        i++) {
      await Future<void>.delayed(const Duration(milliseconds: 20));
      layout2 = container2.read(homeLayoutProvider);
    }
    expect(layout2.hidden, contains(HomeSection.stats));
    expect(layout2.order.last, first);
  });
}
