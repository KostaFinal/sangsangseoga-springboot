package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.infra.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiImageStorageServiceTest {

    private FileStorageService fileStorageService;
    private AiImageStorageService service;

    @BeforeEach
    void setUp() {
        fileStorageService = mock(FileStorageService.class);
        service = new AiImageStorageService(fileStorageService);
    }

    @Test
    void 유효한_data_URI는_디코딩되어_FileStorageService로_저장된다() {
        byte[] rawBytes = {1, 2, 3, 4};
        String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(rawBytes);
        when(fileStorageService.store(eq(rawBytes), eq("image/png"), eq("png"), eq("ai-images/cover")))
                .thenReturn("https://cdn.example.com/ai-images/cover/abc.png");

        AiImageStorageService.StoredImage stored = service.storeFromDataUri(dataUri, "COVER");

        assertThat(stored.getUrl()).isEqualTo("https://cdn.example.com/ai-images/cover/abc.png");
        verify(fileStorageService).store(rawBytes, "image/png", "png", "ai-images/cover");
    }

    @Test
    void PAGE_타입은_ai_images_page_서브디렉터리로_저장된다() {
        byte[] rawBytes = {9};
        String dataUri = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(rawBytes);
        when(fileStorageService.store(any(), any(), any(), any())).thenReturn("https://cdn.example.com/ai-images/page/xyz.jpg");

        AiImageStorageService.StoredImage stored = service.storeFromDataUri(dataUri, "PAGE");

        assertThat(stored.getUrl()).startsWith("https://cdn.example.com/ai-images/page/");
        verify(fileStorageService).store(eq(rawBytes), eq("image/jpeg"), eq("jpg"), eq("ai-images/page"));
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

    @Test
    void deleteQuietly는_FileStorageService_delete를_호출한다() {
        AiImageStorageService.StoredImage stored = new AiImageStorageService.StoredImage("https://cdn.example.com/ai-images/cover/abc.png");

        service.deleteQuietly(stored);

        verify(fileStorageService).delete("https://cdn.example.com/ai-images/cover/abc.png");
    }
}
