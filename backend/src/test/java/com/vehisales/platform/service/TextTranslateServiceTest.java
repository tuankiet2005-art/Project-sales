package com.vehisales.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TextTranslateServiceTest {

    private final TextTranslateService service = new TextTranslateService(new ObjectMapper());

    @Test
    void translatesKnownProvinceFromGlossary() {
        var names = service.translate("Hà Nội");
        assertThat(names.vi()).isEqualTo("Hà Nội");
        assertThat(names.en()).isEqualTo("Hanoi");
        assertThat(names.zh()).isEqualTo("河内");
        assertThat(names.ja()).isEqualTo("ハノイ");
    }

    @Test
    void fillsBlankLanguagesFromVietnamese() {
        var names = service.fillIfNeeded("Đà Nẵng", "", "", "");
        assertThat(names.en()).isEqualTo("Da Nang");
        assertThat(names.zh()).isEqualTo("岘港");
        assertThat(names.ja()).isEqualTo("ダナン");
    }

    @Test
    void replacesCopiedVietnameseWithTranslation() {
        var names = service.fillIfNeeded("Huế", "Huế", "Huế", "Huế");
        assertThat(names.en()).isEqualTo("Hue");
        assertThat(names.zh()).isEqualTo("顺化");
        assertThat(names.ja()).isEqualTo("フエ");
    }

    @Test
    void keepsOperatorEditedLanguage() {
        var names = service.fillIfNeeded("Hà Nội", "Hanoi Capital", "", "");
        assertThat(names.en()).isEqualTo("Hanoi Capital");
        assertThat(names.zh()).isEqualTo("河内");
    }

    @Test
    void completesOfferMapFromVietnamese() {
        Map<String, String> filled = service.completeMap(Map.of("vi", "Phụ kiện tặng theo xe"));
        assertThat(filled.get("en")).isEqualTo("Complimentary accessories");
        assertThat(filled.get("zh")).isEqualTo("随车赠送配件");
        assertThat(filled.get("ja")).isEqualTo("車両付属の贈呈装備");
    }
}
