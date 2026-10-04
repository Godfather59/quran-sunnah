// Library models: bookmarks, notes, highlights, custom collections.

enum BookmarkKind { ayah, hadith, tafsir }

class Bookmark {
  const Bookmark({
    required this.id,
    required this.kind,
    required this.refKey,
    required this.title,
    required this.subtitle,
    this.collectionId,
    this.createdAt,
  });

  final String id;
  final BookmarkKind kind;
  final String refKey; // "2:255" or "bukhari:123"
  final String title;
  final String subtitle;
  final String? collectionId;
  final DateTime? createdAt;
}

class UserNote {
  const UserNote({
    required this.id,
    required this.refKey,
    required this.text,
    this.createdAt,
  });

  final String id;
  final String refKey;
  final String text;
  final DateTime? createdAt;
}

class Highlight {
  const Highlight({
    required this.id,
    required this.refKey,
    required this.colorValue,
  });

  final String id;
  final String refKey;
  final int colorValue;
}

class CustomCollection {
  const CustomCollection({required this.id, required this.name});

  final String id;
  final String name;
}

class RecentItem {
  const RecentItem({
    required this.refKey,
    required this.title,
    required this.subtitle,
    required this.kind,
  });

  final String refKey;
  final String title;
  final String subtitle;
  final BookmarkKind kind;
}
