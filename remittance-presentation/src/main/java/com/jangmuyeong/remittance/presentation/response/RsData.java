package com.jangmuyeong.remittance.presentation.response;

/**
 * 공통 API 응답 포맷
 */
public record RsData<T>(T data) {
	public static <T> RsData<T> of(T data) {
		return new RsData<>(data);
	}
}