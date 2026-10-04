# Data Licensing & Redistribution Audit

Last reviewed: 2026-10-04

This document audits the **bundled content datasets**, separately from the
application source code. It is an engineering compliance record, not legal
advice. A repository-level software license does not automatically establish
rights over third-party Quran editions, translations, tafsir, or Hadith texts
collected by that repository.

Status meanings:

- **CLEAR WITH CONDITIONS** — redistribution terms were found; follow the
  attribution / verbatim / other listed conditions.
- **RESTRICTED** — current source terms impose a material restriction.
- **UNRESOLVED** — the provenance is known but redistribution rights for the
  underlying text are not sufficiently established. Do not broaden
  distribution until the direct upstream terms or permission are recorded.

| Dataset | Provenance | Audit status | Required action / condition |
| --- | --- | --- | --- |
| Hafs Uthmani + Imla'i Quran text | Tanzil Project | **CLEAR WITH CONDITIONS** | Tanzil Quran Text is CC BY 3.0. Keep text verbatim, clearly identify Tanzil as source, link to Tanzil, reproduce the copyright/license notice, and track updates. |
| Warsh Uthmani | Quran API → QuranComplex source | **UNRESOLVED** | The quran-api repository itself uses the Unlicense, but that does not by itself prove redistribution rights for the underlying QuranComplex edition. Record direct QuranComplex terms/permission before broader redistribution. |
| Qalun Uthmani | Quran API → QuranComplex source | **UNRESOLVED** | Same issue as Warsh: repository license is not sufficient evidence for the underlying edition rights. |
| Hafs IndoPak | Quran API → QuranComplex fonts/source | **UNRESOLVED** | Confirm direct upstream text/Unicode conversion redistribution terms. The bundled Amiri Quran font is separately OFL-licensed. |
| Saheeh International translation | Tanzil translations | **RESTRICTED** | Tanzil states translations from its download page are for non-commercial purposes unless permission is obtained from the translator/publisher. Keep translator/source credit. |
| Hamidullah French translation | Tanzil translations | **RESTRICTED** | Same Tanzil translation restriction: non-commercial use unless necessary permission is obtained. Keep translator/source credit. |
| Tafsir al-Jalalayn | Quran API → Tanzil-listed Arabic tafsir | **RESTRICTED / VERIFY DIRECT RIGHTS** | Tanzil lists Jalalayn among downloadable translations/tafsir and applies its translation-page terms, including non-commercial use unless permission exists. Verify the exact upstream edition rights before broader distribution. |
| Al-Siraj tafsir | Quran API → QuranEnc provenance | **CLEAR WITH CONDITIONS, VERSION INCOMPLETE** | QuranEnc allows download/republication if content is not modified; publisher/source and version are stated; transcript information is retained; updates are followed. The current provenance must record the exact QuranEnc version/translation key before treating this as complete. |
| Quranic Arabic Corpus morphology | QAC v0.4 | **CLEAR WITH CONDITIONS** | GPL plus QAC terms. Credit Quranic Arabic Corpus, link to corpus.quran.com, reproduce the relevant copyright/license notice, and preserve the required freedoms/source obligations for covered derived material. |
| Tajweed annotations | cpfair/quran-tajweed decision trees/data | **CLEAR WITH CONDITIONS** | The project states the Tajweed data file is CC BY 4.0. Attribute the project. The Quran text itself remains governed by Tanzil terms. |
| Bundled Arabic Hadith collections | hadith-api editions | **UNRESOLVED FOR UNDERLYING TEXTS** | hadith-api repository uses the Unlicense, but upstream issue #129 (opened 2026-04-09) explicitly asks what the source/license of the Arabic texts is and remains open. Do not assume the repo software license grants rights to all underlying edition texts. |
| Amiri Quran font | Google Fonts / Amiri | **CLEAR WITH CONDITIONS** | SIL Open Font License 1.1. License file is bundled. |
| Noto Naskh Arabic font | Google Fonts / Noto | **CLEAR WITH CONDITIONS** | SIL Open Font License. License file is bundled. |

## Authoritative references

- Tanzil Quran Text license: https://tanzil.net/docs/Text_License
- Tanzil translations terms: https://tanzil.net/trans/
- Quranic Arabic Corpus v0.4 terms: https://corpus.quran.com/download/
- Quranic Arabic Corpus GPL: https://corpus.quran.com/license.jsp
- QuranEnc API/republication terms: https://quranenc.com/
- cpfair/quran-tajweed: https://github.com/cpfair/quran-tajweed
- quran-api repository license: https://github.com/fawazahmed0/quran-api/blob/1/LICENSE
- hadith-api repository license: https://github.com/fawazahmed0/hadith-api/blob/1/LICENSE
- Open hadith-api text-license question: https://github.com/fawazahmed0/hadith-api/issues/129

## Engineering distribution gate

A bundled content row should move from **UNRESOLVED** to **CLEAR WITH
CONDITIONS** only when the repository records:

1. the direct content owner/publisher or authoritative source;
2. the exact edition/version or retrieval identifier;
3. redistribution terms or written permission that apply to the underlying
   content, not merely the aggregator's source code;
4. required attribution text and update obligations; and
5. an integrity fingerprint for the exact bundled bytes.

The application must continue to distinguish Quran text, translations, tafsir,
Hadith, morphology, Tajweed annotations, and fonts because they are governed by
different provenance and licensing terms.
