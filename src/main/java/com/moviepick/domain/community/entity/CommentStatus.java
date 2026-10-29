package com.moviepick.domain.community.entity;

public enum CommentStatus {
    VISIBLE,
    DELETED,
    BLINDED   // 신고 승인으로 관리자가 숨김 (목록에 자리 유지, 원문 미전송)
}
