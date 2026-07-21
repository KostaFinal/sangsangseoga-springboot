package com.kosta.sangsangseoga.domain.friendLibrary.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FriendLibraryErrorCode implements ErrorCode {

    // ===== AuthorFollow =====
    AUTHOR_NOT_FOUND(HttpStatus.NOT_FOUND, "작가를 찾을 수 없습니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인을 팔로우할 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팔로우한 작가입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 정보를 찾을 수 없습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "sort 값이 올바르지 않습니다 (followers/works 중 하나여야 합니다)"),

    // ===== BookLike =====
    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요한 책입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다."),

    // ===== Bookmark =====
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 북마크한 페이지입니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크 정보를 찾을 수 없습니다."),

    // ===== Comment =====
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "답글에는 답글을 달 수 없습니다."),

    // ===== Report =====
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신고한 대상입니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 대상을 찾을 수 없습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    FriendLibraryErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}