package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring DTO(평평한 구조, context.draft/extra 중첩 구조, 또는 Postman이 보낸 Python 스키마 최상위 구조)를
 * Python FastAPI가 기대하는 {taskType, draft:{bookType, meta, setting}, extra} 구조로 변환한다.
 * non-transactional 컴포넌트로 분리해 {@link AiService}와 스트리밍 서비스가 동일 로직을 공유한다.
 */
@Component
public class AiPythonRequestMapper {

	public Map<String, Object> buildPythonRequestBody(AiGenerateRequestDto request) {
		// React 정식 흐름: context.draft / context.extra에 중첩되어 온다.
		Map<String, Object> context = request.getContext();
		Map<String, Object> contextDraft = asMap(context == null ? null : context.get("draft"));
		Map<String, Object> contextMeta = asMap(contextDraft.get("meta"));
		Map<String, Object> contextSetting = asMap(contextDraft.get("setting"));
		Map<String, Object> contextExtra = asMap(context == null ? null : context.get("extra"));

		// Postman이 Python 스키마(taskType/draft/extra 최상위)를 그대로 보낸 경우
		Map<String, Object> topDraft = asMap(request.getDraft());
		Map<String, Object> topMeta = asMap(topDraft.get("meta"));
		Map<String, Object> topSetting = asMap(topDraft.get("setting"));
		Map<String, Object> topSettings = asMap(topDraft.get("settings"));
		Map<String, Object> topExtra = asMap(request.getExtra());

		// meta: React/Postman이 보낸 값을 전체 병합한 뒤, 명시적으로 채워야 하는 값을 override.
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.putAll(contextMeta);
		meta.putAll(topMeta);
		meta.put("readerAge", firstNonNull(
				enumName(request.getAuthorAgeGroup()),
				enumName(request.getReaderAgeGroup()),
				topMeta.get("readerAge"),
				contextMeta.get("readerAge"),
				contextMeta.get("writerLevel")));
		meta.put("interactionMode", firstNonNull(
				enumName(request.getCreationMode()),
				topMeta.get("interactionMode"),
				contextMeta.get("interactionMode")));

		// setting: context.draft.setting / draft.setting(singular) / draft.settings(plural) 전체를 병합.
		Map<String, Object> setting = new LinkedHashMap<>();
		setting.putAll(contextSetting);
		setting.putAll(topSettings);
		setting.putAll(topSetting);
		setting.put("storySeed", firstNonNull(request.getStorySeed(), topSetting.get("storySeed"), topSettings.get("storySeed"), contextSetting.get("storySeed")));
		setting.put("protagonistName", firstNonNull(request.getProtagonistName(), topSetting.get("protagonistName"), topSettings.get("protagonistName"), contextSetting.get("protagonistName")));
		setting.put("backgroundPlace", firstNonNull(request.getBackgroundPlace(), topSetting.get("backgroundPlace"), topSettings.get("backgroundPlace"), contextSetting.get("backgroundPlace")));
		setting.put("problem", firstNonNull(request.getProblem(), topSetting.get("problem"), topSettings.get("problem"), contextSetting.get("problem")));

		// draft: context.draft / 최상위 draft 전체를 병합한 뒤 bookType/meta/setting을 override.
		Map<String, Object> draft = new LinkedHashMap<>();
		draft.putAll(contextDraft);
		draft.putAll(topDraft);
		draft.put("bookType", firstNonNull(enumName(request.getBookType()), topDraft.get("bookType"), contextDraft.get("bookType")));
		draft.put("meta", meta);
		draft.put("setting", setting);
		draft.put("settings", setting);

		// extra: context.extra / 최상위 extra 전체를 병합(previousAnswers, currentStepKey 등 임의 필드 보존).
		Map<String, Object> extra = new LinkedHashMap<>();
		extra.putAll(contextExtra);
		extra.putAll(topExtra);
		extra.put("userMessage", firstNonNull(
				request.getPrompt(),
				request.getMessage(),
				topExtra.get("userMessage"),
				contextExtra.get("userMessage"),
				contextExtra.get("message")));

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("taskType", request.getStage());
		body.put("draft", draft);
		body.put("extra", extra);
		return body;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map ? (Map<String, Object>) value : Map.of();
	}

	private String enumName(Enum<?> value) {
		return value == null ? null : value.name();
	}

	private Object firstNonNull(Object... values) {
		for (Object value : values) {
			if (value != null && !"".equals(value)) {
				return value;
			}
		}
		return null;
	}
}
