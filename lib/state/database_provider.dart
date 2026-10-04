import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/database/app_database.dart';
import '../data/database/library_store.dart';

final appDatabaseProvider = Provider<Future<AppDatabase>>((ref) async {
  final db = AppDatabase();
  ref.onDispose(db.close);
  return db;
});

final libraryStoreProvider = Provider<Future<LibraryStore>>((ref) async {
  final db = await ref.watch(appDatabaseProvider);
  final store = LibraryStore(db);
  await store.initialize();
  return store;
});
