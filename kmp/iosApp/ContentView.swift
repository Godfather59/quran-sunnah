import Foundation
import SwiftUI
import QuranSunnahShared

private struct Surah: Identifiable, Hashable, Sendable, Decodable {
    let id: Int
    let arabicName: String
    let transliteration: String
    let englishName: String
    let ayahCount: Int

    var displayName: String {
        transliteration == englishName ? transliteration : "\(transliteration) · \(englishName)"
    }

    enum CodingKeys: String, CodingKey {
        case id = "number"
        case arabicName, transliteration, englishName, ayahCount
    }
}

private struct Verse: Identifiable, Hashable, Sendable, Decodable {
    let surah: Int
    let number: Int
    let text: String
    var id: String { "\(surah):\(number)" }
}

private struct QuranData: Sendable {
    let surahs: [Surah]
    let versesBySurah: [Int: [Verse]]
}

private struct QuranCatalog: Decodable, Sendable {
    let surahs: [Surah]
    let ayahs: [Verse]
}

private struct QuranSearchHit: Decodable, Identifiable, Sendable {
    let title: String
    let subtitle: String
    let snippet: String
    let surah: Int
    let ayah: Int
    var id: String { "\(surah):\(ayah)" }
}

/// Uses the shared Kotlin loader and search engine over the verified bundle assets.
@MainActor
private final class QuranLibrary: ObservableObject {
    @Published private(set) var surahs: [Surah] = []
    @Published private(set) var versesBySurah: [Int: [Verse]] = [:]
    @Published private(set) var loadError: String?
    @Published private(set) var isLoading = false
    @Published private(set) var searchResults: [QuranSearchHit] = []
    @Published private(set) var isSearching = false
    @Published private(set) var searchError: String?
    private var searchRequest = UUID()

    func loadIfNeeded() async {
        guard surahs.isEmpty, !isLoading else { return }
        isLoading = true
        let data = await Task.detached(priority: .userInitiated) {
            Self.decodeCatalog(IosQuranApi.shared.catalogJson())
        }.value
        surahs = data?.surahs ?? []
        versesBySurah = data?.versesBySurah ?? [:]
        loadError = data == nil ? String(localized: "contentUnavailable") : nil
        isLoading = false
    }

    func search(_ query: String) async {
        let request = UUID()
        searchRequest = request
        searchResults = []
        searchError = nil
        let trimmedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines)
        isSearching = !trimmedQuery.isEmpty
        guard isSearching else { return }
        // The debounce belongs to the request, so new input clears old hits immediately.
        do { try await Task.sleep(for: .milliseconds(250)) } catch { return }
        guard !Task.isCancelled, searchRequest == request else { return }
        let json = await Task.detached(priority: .userInitiated) {
            IosQuranApi.shared.searchQuranJson(query: trimmedQuery)
        }.value
        // Detached Kotlin calls can finish after cancellation; never publish a stale response.
        guard !Task.isCancelled, searchRequest == request else { return }
        do {
            searchResults = try JSONDecoder().decode([QuranSearchHit].self, from: Data(json.utf8))
        } catch {
            searchError = String(localized: "searchFailed")
        }
        isSearching = false
    }

    private nonisolated static func decodeCatalog(_ json: String) -> QuranData? {
        guard let catalog = try? JSONDecoder().decode(QuranCatalog.self, from: Data(json.utf8)),
              catalog.surahs.count == 114 else { return nil }
        var versesBySurah: [Int: [Verse]] = [:]
        for verse in catalog.ayahs {
            versesBySurah[verse.surah, default: []].append(verse)
        }
        guard versesBySurah.values.reduce(0, { $0 + $1.count }) == 6236 else { return nil }
        return QuranData(surahs: catalog.surahs, versesBySurah: versesBySurah)
    }
}

private struct ReaderRoute: Hashable {
    let surah: Int
    let ayah: Int?
}

struct ContentView: View {
    @StateObject private var library = QuranLibrary()
    @State private var path: [ReaderRoute] = []
    @State private var query = ""
    @AppStorage("q.lastSurah") private var lastSurah = 0
    @AppStorage("q.lastAyah") private var lastAyah = 1

    private var normalizedQuery: String { query.trimmingCharacters(in: .whitespacesAndNewlines) }

    private var matchingSurahs: [Surah] {
        library.surahs.filter { surah in
            normalizedQuery.isEmpty || surah.displayName.localizedCaseInsensitiveContains(normalizedQuery) ||
                surah.arabicName.contains(normalizedQuery) || "\(surah.id)" == normalizedQuery
        }
    }

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                if library.isLoading {
                    ProgressView()
                } else if let error = library.loadError {
                    VStack(spacing: 12) {
                        Image(systemName: "book.closed")
                            .font(.largeTitle)
                            .foregroundStyle(.secondary)
                        Text(error)
                            .multilineTextAlignment(.center)
                            .foregroundStyle(.secondary)
                        Button(String(localized: "retry")) {
                            Task { await library.loadIfNeeded() }
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        if normalizedQuery.isEmpty,
                           let resumeSurah = library.surahs.first(where: { $0.id == lastSurah }) {
                            Section {
                                NavigationLink(value: ReaderRoute(surah: lastSurah, ayah: lastAyah)) {
                                    VStack(alignment: .leading, spacing: 6) {
                                        Label(String(localized: "continueReading"), systemImage: "book")
                                            .font(.headline)
                                        Text("\(resumeSurah.displayName) · \(String(localized: "ayahLabel")) \(lastAyah)")
                                            .font(.subheadline)
                                            .foregroundStyle(.secondary)
                                    }
                                    .padding(.vertical, 8)
                                }
                            }
                        }
                        if !normalizedQuery.isEmpty, library.isSearching {
                            ProgressView(String(localized: "searching"))
                                .frame(maxWidth: .infinity, alignment: .center)
                        } else if !normalizedQuery.isEmpty, let error = library.searchError {
                            VStack(alignment: .leading, spacing: 10) {
                                Text(error).foregroundStyle(.secondary)
                                Button(String(localized: "retry")) {
                                    Task { await library.search(query) }
                                }
                            }
                        } else if !normalizedQuery.isEmpty, library.searchResults.isEmpty, matchingSurahs.isEmpty {
                            VStack(alignment: .leading, spacing: 8) {
                                Label(String(localized: "noSearchResults"), systemImage: "magnifyingglass")
                                    .font(.headline)
                                Text(String(localized: "searchNoResultsHint"))
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                            }
                            .padding(.vertical, 12)
                        }
                        if !normalizedQuery.isEmpty, !library.searchResults.isEmpty {
                            Section(String(localized: "searchResults")) {
                                ForEach(library.searchResults) { hit in
                                    NavigationLink(value: ReaderRoute(surah: hit.surah, ayah: hit.ayah)) {
                                        VStack(alignment: .leading, spacing: 5) {
                                            Text("\(hit.title) · \(hit.subtitle)")
                                                .font(.headline)
                                            Text(hit.snippet)
                                                .font(.body)
                                                .lineLimit(2)
                                                .multilineTextAlignment(.leading)
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .environment(\.layoutDirection, .rightToLeft)
                                        }
                                    }
                                }
                            }
                        }
                        if !matchingSurahs.isEmpty {
                          Section(String(localized: "surahs")) {
                            ForEach(matchingSurahs) { surah in
                                NavigationLink(value: ReaderRoute(surah: surah.id, ayah: nil)) {
                                    HStack(spacing: 14) {
                                        Text("\(surah.id)")
                                            .font(.caption.monospacedDigit())
                                            .foregroundStyle(.secondary)
                                            .frame(width: 32, alignment: .leading)
                                        VStack(alignment: .leading, spacing: 4) {
                                            Text(surah.displayName).font(.headline)
                                            Text("\(surah.ayahCount) · \(String(localized: "ayahLabel"))")
                                                .font(.caption).foregroundStyle(.secondary)
                                        }
                                        Spacer(minLength: 8)
                                        Text(surah.arabicName)
                                            .font(.title3)
                                            .environment(\.layoutDirection, .rightToLeft)
                                    }
                                    .padding(.vertical, 5)
                                }
                            }
                          }
                        }
                    }
                    .listStyle(.plain)
                    .searchable(text: $query, prompt: String(localized: "search"))
                }
            }
            .navigationTitle(String(localized: "quran"))
            .navigationDestination(for: ReaderRoute.self) { route in
                QuranReaderView(
                    surah: library.surahs.first { $0.id == route.surah },
                    verses: library.versesBySurah[route.surah] ?? [],
                    initialAyah: route.ayah
                )
            }
        }
        .task { await library.loadIfNeeded() }
        .task(id: query) { await library.search(query) }
        .onOpenURL(perform: openDeepLink)
    }

    private func openDeepLink(_ url: URL) {
        guard url.scheme == "quran" else { return }
        let components = [url.host].compactMap { $0 } + url.pathComponents.filter { $0 != "/" }
        guard let first = components.first,
              let surah = Int(first), (1...114).contains(surah) else { return }
        let requestedAyah = components.dropFirst().first.flatMap(Int.init)
        let ayah = requestedAyah.map { max(1, $0) }
        path = [ReaderRoute(surah: surah, ayah: ayah)]
    }
}

private struct QuranReaderView: View {
    let surah: Surah?
    let verses: [Verse]
    let initialAyah: Int?
    @AppStorage("q.lastSurah") private var lastSurah = 0
    @AppStorage("q.lastAyah") private var lastAyah = 1
    @AppStorage("q.fontSize") private var fontSize = 27.0
    @AppStorage("q.lineHeight") private var lineHeight = 1.6
    @AppStorage("q.showTr") private var showTranslation = false
    @AppStorage("q.translations") private var translationID = "en-sahih"
    @State private var showingSettings = false
    @State private var selectedVerse: Verse?
    @State private var translations: [Int: String] = [:]
    @State private var translationLoading = false
    @State private var translationUnavailable = false
    @State private var restoredPosition = false
    @ScaledMetric(relativeTo: .body) private var spacingScale = 1.0

    private var targetAyah: Int { min(max(initialAyah ?? 1, 1), max(verses.count, 1)) }
    private var translationChoice: String { translationID.split(separator: "\u{1F}").first.map(String.init) ?? "en-sahih" }

    var body: some View {
        ScrollViewReader { proxy in
          GeometryReader { viewport in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 18) {
                    if let surah {
                        Text("Tanzil · Hafs ‘an Asim · Uthmani")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity)
                        Text(surah.arabicName)
                            .font(.largeTitle.weight(.semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                    }
                    if showTranslation, translationLoading {
                        ProgressView().frame(maxWidth: .infinity)
                    } else if showTranslation, translationUnavailable {
                        Text(String(localized: "contentUnavailable"))
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity)
                    }
                    ForEach(verses) { verse in
                        VerseRow(verse: verse, fontSize: fontSize,
                            lineSpacing: max(0, lineHeight - 1) * fontSize * spacingScale,
                            translation: showTranslation ? translations[verse.number] : nil,
                            translator: translationChoice == "fr-hamidullah" ? "Muhammad Hamidullah" : "Saheeh International",
                            onTafsir: { selectedVerse = verse })
                        .padding(.horizontal)
                        .padding(.bottom, 8)
                        .id(verse.id)
                        .background {
                            GeometryReader { geometry in
                                Color.clear.preference(key: VerseFramesKey.self,
                                    value: [verse.number: geometry.frame(in: .named("quranReader"))])
                            }
                        }
                    }
                }
                .padding(.vertical)
            }
            .coordinateSpace(name: "quranReader")
            .onPreferenceChange(VerseFramesKey.self) { frames in
                let visible = frames.filter { $0.value.maxY > 0 && $0.value.minY < viewport.size.height }
                if !restoredPosition {
                    guard visible[targetAyah] != nil else { return }
                    restoredPosition = true
                }
                guard let surah,
                      let first = visible.min(by: { $0.value.minY < $1.value.minY }) else { return }
                if lastSurah != surah.id { lastSurah = surah.id }
                if lastAyah != first.key { lastAyah = first.key }
            }
            .navigationTitle(surah?.displayName ?? String(localized: "quran"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { showingSettings = true } label: {
                        Image(systemName: "textformat.size")
                    }
                    .accessibilityLabel(String(localized: "readerSettings"))
                }
            }
            .sheet(isPresented: $showingSettings) {
                ReaderSettingsView(previewVerse: verses.first, fontSize: $fontSize, lineHeight: $lineHeight,
                    showTranslation: $showTranslation, translationID: $translationID)
            }
            .sheet(item: $selectedVerse) { verse in
                TafsirView(verse: verse)
            }
            .task(id: "\(surah?.id ?? 0):\(showTranslation):\(translationChoice)") {
                translations = [:]
                translationUnavailable = false
                translationLoading = false
                guard showTranslation, let surah else { return }
                translationLoading = true
                let id = translationChoice
                let number = surah.id
                let json = await Task.detached(priority: .userInitiated) {
                    IosQuranApi.shared.translationSurahJson(translationId: id, surah: Int32(number))
                }.value
                guard !Task.isCancelled else { return }
                let rows = (try? JSONDecoder().decode([VerseSupplement].self, from: Data(json.utf8))) ?? []
                translations = Dictionary(rows.map { ($0.ayah, $0.text) }, uniquingKeysWith: { first, _ in first })
                translationUnavailable = rows.isEmpty
                translationLoading = false
            }
            .task(id: "\(surah?.id ?? 0):\(initialAyah ?? 0):\(verses.count)") {
                guard let surah, !verses.isEmpty else { return }
                restoredPosition = false
                lastSurah = surah.id
                lastAyah = targetAyah
                await Task.yield()
                proxy.scrollTo("\(surah.id):\(targetAyah)", anchor: .top)
            }
          }
        }
    }
}

private struct VerseRow: View {
    let verse: Verse
    let fontSize: Double
    let lineSpacing: Double
    let translation: String?
    let translator: String
    let onTafsir: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .firstTextBaseline, spacing: 10) {
                Text(verse.text)
                    .font(.custom("AmiriQuran-Regular", size: fontSize, relativeTo: .body))
                    .lineSpacing(lineSpacing)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text("\(verse.number)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .fixedSize()
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.secondary.opacity(0.12), in: Capsule())
            }
            // Leading is the physical right edge in this Arabic layout,
            // regardless of the language used by the surrounding controls.
            .environment(\.layoutDirection, .rightToLeft)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("\(String(localized: "ayahLabel")) \(verse.surah):\(verse.number). \(verse.text)")
            if let translation {
                VStack(alignment: .leading, spacing: 5) {
                    Text(String(localized: "translationLabel"))
                        .font(.caption).foregroundStyle(.secondary)
                    Text(translation).font(.body)
                    Text(translator).font(.caption).foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .multilineTextAlignment(.leading)
                .environment(\.layoutDirection, .leftToRight)
            }
            Button(action: onTafsir) {
                Label(String(localized: "tafsir"), systemImage: "text.book.closed")
                    .font(.subheadline)
                    .frame(minHeight: 44)
            }
            .buttonStyle(.borderless)
            .accessibilityLabel("\(String(localized: "tafsir")) · \(String(localized: "ayahLabel")) \(verse.number)")
        }
    }
}

private struct VerseFramesKey: PreferenceKey {
    static var defaultValue: [Int: CGRect] = [:]
    static func reduce(value: inout [Int: CGRect], nextValue: () -> [Int: CGRect]) {
        value.merge(nextValue(), uniquingKeysWith: { _, new in new })
    }
}

private struct VerseSupplement: Decodable, Sendable {
    let ayah: Int
    let text: String
}

private struct ReaderSettingsView: View {
    let previewVerse: Verse?
    @Binding var fontSize: Double
    @Binding var lineHeight: Double
    @Binding var showTranslation: Bool
    @Binding var translationID: String
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section(String(localized: "fontSize")) {
                    Slider(value: $fontSize, in: 20...44, step: 1) {
                        Text(String(localized: "fontSize"))
                    }
                    .accessibilityValue("\(Int(fontSize))")
                    if let previewVerse {
                        Text(previewVerse.text)
                            .font(.custom("AmiriQuran-Regular", size: fontSize, relativeTo: .body))
                            .lineSpacing(max(0, lineHeight - 1) * fontSize)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .multilineTextAlignment(.leading)
                            .environment(\.layoutDirection, .rightToLeft)
                            .padding(.vertical, 10)
                    } else {
                        Text(String(localized: "contentUnavailable"))
                            .foregroundStyle(.secondary)
                    }
                }
                Section(String(localized: "lineHeight")) {
                    Slider(value: $lineHeight, in: 1.0...2.0, step: 0.1) {
                        Text(String(localized: "lineHeight"))
                    }
                    .accessibilityValue(lineHeight.formatted(.number.precision(.fractionLength(1))))
                }
                Section(String(localized: "translation")) {
                    Toggle(String(localized: "showTranslation"), isOn: $showTranslation)
                    if showTranslation {
                        Picker(String(localized: "translation"), selection: $translationID) {
                            Text("English · Saheeh International").tag("en-sahih")
                            Text("Français · Muhammad Hamidullah").tag("fr-hamidullah")
                        }
                    }
                }
            }
            .navigationTitle(String(localized: "readerSettings"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(String(localized: "close")) { dismiss() }
                }
            }
        }
    }
}

private struct TafsirView: View {
    let verse: Verse
    @State private var text: String?
    @State private var loading = true
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("\(String(localized: "ayahLabel")) \(verse.surah):\(verse.number)")
                        .font(.subheadline).foregroundStyle(.secondary)
                    Text(verse.text)
                        .font(.custom("AmiriQuran-Regular", size: 27, relativeTo: .body))
                    Divider()
                    Text("تفسير الجلالين").font(.headline)
                    if loading {
                        ProgressView().frame(maxWidth: .infinity)
                    } else {
                        Text(text ?? String(localized: "contentUnavailable"))
                            .font(.custom("NotoNaskhArabic-Regular", size: 22, relativeTo: .body))
                            .textSelection(.enabled)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .multilineTextAlignment(.leading)
                .environment(\.layoutDirection, .rightToLeft)
                .padding()
            }
            .navigationTitle(String(localized: "tafsir"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(String(localized: "close")) { dismiss() }
                }
            }
            .task(id: verse.id) {
                loading = true
                let surah = verse.surah
                let json = await Task.detached(priority: .userInitiated) {
                    IosQuranApi.shared.tafsirSurahJson(tafsirId: "jalalayn", surah: Int32(surah))
                }.value
                guard !Task.isCancelled else { return }
                let rows = try? JSONDecoder().decode([VerseSupplement].self, from: Data(json.utf8))
                text = rows?.first(where: { $0.ayah == verse.number })?.text
                loading = false
            }
        }
    }
}
