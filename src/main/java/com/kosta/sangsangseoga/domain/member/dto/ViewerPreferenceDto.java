package com.kosta.sangsangseoga.domain.member.dto;

import com.kosta.sangsangseoga.domain.member.enums.ViewerFontSize;
import com.kosta.sangsangseoga.domain.member.enums.ViewerViewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewerPreferenceDto {

    private ViewerFontSize viewerFontSize;
    private ViewerViewType viewerViewType;
}
