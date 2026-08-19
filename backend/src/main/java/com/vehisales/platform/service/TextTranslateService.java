package com.vehisales.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehisales.platform.api.dto.admin.TranslateResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TextTranslateService {

    private static final Map<String, TranslateResponse> GLOSSARY = glossary();

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, TranslateResponse> cache = new ConcurrentHashMap<>();

    public TextTranslateService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    public TranslateResponse translate(String vietnamese) {
        String source = trim(vietnamese);
        if (source.isEmpty()) {
            return new TranslateResponse("", "", "", "");
        }
        TranslateResponse known = GLOSSARY.get(normalize(source));
        if (known != null) {
            return known;
        }
        return cache.computeIfAbsent(normalize(source), ignored -> remoteTranslate(source));
    }

    public TranslateResponse fillIfNeeded(String vietnamese, String english, String chinese, String japanese) {
        String source = trim(vietnamese);
        if (source.isEmpty()) {
            return new TranslateResponse("", trim(english), trim(chinese), trim(japanese));
        }
        TranslateResponse translated = translate(source);
        return new TranslateResponse(
                source,
                keepOrFill(english, source, translated.en()),
                keepOrFill(chinese, source, translated.zh()),
                keepOrFill(japanese, source, translated.ja())
        );
    }

    public Map<String, String> completeMap(Map<String, String> values) {
        Map<String, String> source = values == null ? Map.of() : values;
        TranslateResponse filled = fillIfNeeded(
                source.get("vi"),
                source.get("en"),
                source.get("zh"),
                source.get("ja")
        );
        Map<String, String> next = new LinkedHashMap<>();
        next.put("vi", filled.vi());
        next.put("en", filled.en());
        next.put("zh", filled.zh());
        next.put("ja", filled.ja());
        return next;
    }

    private TranslateResponse remoteTranslate(String source) {
        return new TranslateResponse(
                source,
                remote(source, "en", source),
                remote(source, "zh-CN", source),
                remote(source, "ja", source)
        );
    }

    private String remote(String source, String target, String fallback) {
        try {
            String query = URLEncoder.encode(source, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mymemory.translated.net/get?q=" + query + "&langpair=vi|" + target))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return fallback;
            }
            JsonNode text = objectMapper.readTree(response.body()).path("responseData").path("translatedText");
            String translated = text.asText("");
            return translated.isBlank() ? fallback : translated.trim();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String keepOrFill(String current, String vietnamese, String translated) {
        String value = trim(current);
        if (value.isEmpty() || value.equals(vietnamese)) {
            return translated == null || translated.isBlank() ? vietnamese : translated;
        }
        return value;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private static Map<String, TranslateResponse> glossary() {
        Map<String, TranslateResponse> map = new LinkedHashMap<>();
        put(map, "Hà Nội", "Hanoi", "河内", "ハノイ");
        put(map, "Hải Phòng", "Hai Phong", "海防", "ハイフォン");
        put(map, "Bắc Ninh", "Bac Ninh", "北宁", "バクニン");
        put(map, "Cao Bằng", "Cao Bang", "高平", "カオバン");
        put(map, "Điện Biên", "Dien Bien", "奠边", "ディエンビエン");
        put(map, "Hưng Yên", "Hung Yen", "兴安", "フンイエン");
        put(map, "Lai Châu", "Lai Chau", "莱州", "ライチャウ");
        put(map, "Lạng Sơn", "Lang Son", "谅山", "ランソン");
        put(map, "Lào Cai", "Lao Cai", "老街", "ラオカイ");
        put(map, "Ninh Bình", "Ninh Binh", "宁平", "ニンビン");
        put(map, "Phú Thọ", "Phu Tho", "富寿", "フート");
        put(map, "Quảng Ninh", "Quang Ninh", "广宁", "クアンニン");
        put(map, "Sơn La", "Son La", "山罗", "ソンラ");
        put(map, "Thái Nguyên", "Thai Nguyen", "太原", "タイグエン");
        put(map, "Tuyên Quang", "Tuyen Quang", "宣光", "トゥエンクアン");
        put(map, "Huế", "Hue", "顺化", "フエ");
        put(map, "Đà Nẵng", "Da Nang", "岘港", "ダナン");
        put(map, "Hà Tĩnh", "Ha Tinh", "河静", "ハティン");
        put(map, "Khánh Hòa", "Khanh Hoa", "庆和", "カインホア");
        put(map, "Nghệ An", "Nghe An", "义安", "ゲアン");
        put(map, "Quảng Ngãi", "Quang Ngai", "广义", "クアンガイ");
        put(map, "Quảng Trị", "Quang Tri", "广治", "クアンチ");
        put(map, "Thanh Hóa", "Thanh Hoa", "清化", "タインホア");
        put(map, "Gia Lai", "Gia Lai", "嘉莱", "ザライ");
        put(map, "Đắk Lắk", "Dak Lak", "得乐", "ダクラク");
        put(map, "Lâm Đồng", "Lam Dong", "林同", "ラムドン");
        put(map, "Thành phố Hồ Chí Minh", "Ho Chi Minh City", "胡志明市", "ホーチミン市");
        put(map, "TP. Hồ Chí Minh", "Ho Chi Minh City", "胡志明市", "ホーチミン市");
        put(map, "Cần Thơ", "Can Tho", "芹苴", "カントー");
        put(map, "An Giang", "An Giang", "安江", "アンザン");
        put(map, "Cà Mau", "Ca Mau", "金瓯", "カマウ");
        put(map, "Đồng Nai", "Dong Nai", "同奈", "ドンナイ");
        put(map, "Đồng Tháp", "Dong Thap", "同塔", "ドンタップ");
        put(map, "Tây Ninh", "Tay Ninh", "西宁", "タイニン");
        put(map, "Vĩnh Long", "Vinh Long", "永隆", "ヴィンロン");
        put(map, "Phụ kiện tặng theo xe", "Complimentary accessories", "随车赠送配件", "車両付属の贈呈装備");
        put(map, "Chương trình khuyến mại tháng", "This month's campaign", "本月促销", "今月のキャンペーン");
        return Map.copyOf(map);
    }

    private static void put(Map<String, TranslateResponse> map, String vi, String en, String zh, String ja) {
        map.put(normalize(vi), new TranslateResponse(vi, en, zh, ja));
    }
}
