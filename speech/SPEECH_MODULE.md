# Speech Module

Module Android library cung cấp các chức năng liên quan đến giọng nói cho ứng dụng học ngôn ngữ.

## Tổng quan

Module `speech` bao gồm 3 thành phần chính:

| Thành phần | Mục đích |
|---|---|
| **STT (Speech-to-Text)** | Nhận dạng giọng nói thành văn bản |
| **TTS (Text-to-Speech)** | Chuyển văn bản thành giọng nói |
| **Pronunciation Assessment** | Đánh giá phát âm của người dùng |

## Cấu trúc thư mục

```
speech/
├── di/
│   └── SpeechModule.kt              # Koin DI module
├── stt/
│   ├── SpeechToTextProvider.kt       # Abstract class cho STT
│   ├── SttConfig.kt                  # Cấu hình: locale, partial results, max results
│   ├── SttResult.kt                  # Sealed interface SttEvent (Result, PartialResult, Error...)
│   └── android/
│       └── AndroidSpeechToTextProvider.kt   # Hiện thực bằng Android SpeechRecognizer
├── tts/
│   ├── TextToSpeechProvider.kt       # Abstract class cho TTS
│   ├── TtsConfig.kt                  # Cấu hình: locale, pitch, speechRate, voiceIndex
│   ├── TtsEvent.kt                   # Sealed interface TtsEvent (Start, Done, Error)
│   └── android/
│       └── AndroidTextToSpeechProvider.kt   # Hiện thực bằng Android TextToSpeech (Google TTS engine)
└── pronunciation/
    ├── PronunciationAssessor.kt      # Abstract class cho đánh giá phát âm
    ├── PronunciationResult.kt        # Kết quả: score, level, matched/unmatched words, word similarity
    ├── SttMetadata.kt                # Metadata từ STT (confidence, alternatives) dùng cho assessment
    └── simple/
        └── SimpleWordMatchAssessor.kt # Đánh giá phát âm: fuzzy matching, word order, STT confidence
```

## Chi tiết từng thành phần

### 1. Speech-to-Text (STT)

**`SpeechToTextProvider`** — abstract class định nghĩa interface chung:
- `startListening(config)` — bắt đầu nhận dạng giọng nói
- `stopListening()` — dừng nhận dạng
- `cancel()` / `destroy()` — huỷ và giải phóng tài nguyên
- `events: Flow<SttEvent>` — stream sự kiện realtime (kết quả, lỗi, trạng thái)

**`AndroidSpeechToTextProvider`** — hiện thực sử dụng `android.speech.SpeechRecognizer`:
- Hỗ trợ partial results (kết quả trung gian khi đang nói)
- Trả về alternatives và confidence score
- Map đầy đủ các error code (NETWORK, AUDIO, SERVER, NO_MATCH, v.v.)

**Cấu hình** (`SttConfig`):
- `locale` — ngôn ngữ nhận dạng (mặc định: `Locale.US`)
- `partialResults` — bật/tắt kết quả trung gian
- `maxResults` — số lượng kết quả tối đa

### 2. Text-to-Speech (TTS)

**`TextToSpeechProvider`** — abstract class định nghĩa interface chung:
- `speak(text, queueMode)` — đọc một đoạn văn bản
- `speakSequence(texts)` — đọc nhiều đoạn văn bản liên tiếp
- `stop()` / `destroy()` — dừng và giải phóng tài nguyên
- `events: Flow<TtsEvent>` — stream sự kiện (Start, Done, Error)

**`AndroidTextToSpeechProvider`** — hiện thực sử dụng `android.speech.tts.TextToSpeech`:
- Sử dụng Google TTS engine (`com.google.android.tts`)
- Hỗ trợ chọn voice theo index
- Tự động clean text trước khi đọc (loại bỏ markdown, ký tự đặc biệt)
- Xử lý riêng cho ngôn ngữ CJK (Trung, Nhật, Hàn)
- Queue mode: `FLUSH` (thay thế) hoặc `ADD` (nối tiếp)

**Cấu hình** (`TtsConfig`):
- `locale` — ngôn ngữ đọc
- `pitch` — cao độ giọng (mặc định: 1.0)
- `speechRate` — tốc độ đọc (mặc định: 0.95)
- `voiceIndex` — chọn giọng cụ thể

**Tốc độ đọc** (`TtsSpeed`): NORMAL (0.95), SLOW (0.7), VERY_SLOW (0.45)

### 3. Pronunciation Assessment

**`PronunciationAssessor`** — abstract class:
- `assess(referenceText, spokenText, locale, sttMetadata?)` — so sánh văn bản gốc với lời nói của người dùng
- `normalizeText()` — chuẩn hoá text (lowercase, loại bỏ ký tự đặc biệt, tách từ)

**`SttMetadata`** — metadata từ kết quả STT, dùng để cải thiện độ chính xác assessment:
- `confidence` — confidence score từ SpeechRecognizer (-1 nếu không có)
- `alternatives` — danh sách kết quả nhận dạng thay thế

**`SimpleWordMatchAssessor`** — đánh giá phát âm dựa trên 4 thành phần:

| Thành phần | Trọng số | Mô tả |
|---|---|---|
| **Word match** (fuzzy) | 70% | So khớp từ bằng Levenshtein distance, threshold ≥ 0.75 (ví dụ: "kat" ≈ "cat") |
| **Word order** (LCS) | 30% | Kiểm tra thứ tự từ bằng Longest Common Subsequence |
| **Confidence penalty** | tối đa -15 | Confidence STT < 0.7 → phát âm không rõ, bị trừ điểm |
| **Auto-correct penalty** | tối đa -15 | Alternatives khác nhiều so với best result → Google đã auto-correct nhiều |

Tổng penalty tối đa 25 điểm. Công thức:
```
rawScore = wordMatchScore × 0.7 + orderScore × 0.3
finalScore = rawScore - min(confidencePenalty + autoCorrectPenalty, 25)
```

**Kết quả** (`PronunciationResult`):
- `score` — điểm tổng 0-100 (sau khi áp dụng trọng số và penalty)
- `level` — xếp loại: EXCELLENT (>=90), GOOD (>=70), FAIR (>=50), TRY_AGAIN (<50)
- `matchedWords` / `unmatchedWords` — danh sách từ khớp và không khớp
- `wordDetails` — chi tiết từng từ kèm `similarity` (0-1, mức độ tương đồng fuzzy)
- `wordMatchScore` — điểm thành phần word match (0-100)
- `orderScore` — điểm thành phần thứ tự từ (0-100)

**Cách sử dụng với STT metadata** (khuyến nghị):
```kotlin
// Lấy kết quả STT
val sttResult: SttEvent.Result = ...

// Truyền metadata để assessment chính xác hơn
assessor.assess(
    referenceText = "the cat sat on the mat",
    spokenText = sttResult.text,
    sttMetadata = SttMetadata(
        confidence = sttResult.confidence,
        alternatives = sttResult.alternatives,
    ),
)
```

Nếu không truyền `sttMetadata`, assessor vẫn hoạt động bình thường nhưng không có confidence/auto-correct penalty.

## Dependency Injection (Koin)

```kotlin
val speechModule = module {
    factoryOf(::AndroidSpeechToTextProvider) bind SpeechToTextProvider::class
    singleOf(::AndroidTextToSpeechProvider) bind TextToSpeechProvider::class
    singleOf(::SimpleWordMatchAssessor) bind PronunciationAssessor::class
}
```

- **STT**: `factory` — tạo instance mới mỗi lần inject (vì SpeechRecognizer không nên tái sử dụng)
- **TTS**: `single` — dùng chung 1 instance (cần initialize 1 lần, dùng lại nhiều lần)
- **PronunciationAssessor**: `single` — stateless, dùng chung được

## Dependencies

- `kotlinx-coroutines-core` — Flow cho event streaming
- `koin-core` — Dependency injection
