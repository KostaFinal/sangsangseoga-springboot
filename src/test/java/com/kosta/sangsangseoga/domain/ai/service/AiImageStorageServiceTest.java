package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.global.config.AppProperties;
import com.kosta.sangsangseoga.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private AiImageStorageService service;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getUpload().setImageDir(tempDir.toString());
        appProperties.getUpload().setImageUrl("/ai-images");
        service = new AiImageStorageService(appProperties);
    }

    @Test
    void 유효한_data_URI는_디코딩되어_로컬에_저장된다() throws IOException {
        byte[] rawBytes = {1, 2, 3, 4};
        String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(rawBytes);

        AiImageStorageService.StoredImage stored = service.storeFromDataUri(dataUri, "COVER");

        assertThat(stored.getFile()).exists();
        assertThat(stored.getFile().toString()).endsWith(".png");
        assertThat(stored.getRelativeUrl()).startsWith("/ai-images/cover/");
        assertThat(Files.readAllBytes(stored.getFile())).isEqualTo(rawBytes);
    }

    @Test
    void PAGE_타입은_page_서브디렉터리에_저장된다() {
        String dataUri = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[]{9});

        AiImageStorageService.StoredImage stored = service.storeFromDataUri(dataUri, "PAGE");

        assertThat(stored.getRelativeUrl()).startsWith("/ai-images/page/");
        assertThat(stored.getFile().toString()).endsWith(".jpg");
    }

    @Test
    void data_URI_형식이_아니면_IMAGE_SAVE_FAILED() {
        assertThatThrownBy(() -> service.storeFromDataUri("not-a-data-uri", "COVER"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void base64_디코딩이_실패하면_IMAGE_SAVE_FAILED() {
        assertThatThrownBy(() -> service.storeFromDataUri("data:image/png;base64,***invalid***", "COVER"))
                .isInstanceOf(CustomException.class);
    }
}
