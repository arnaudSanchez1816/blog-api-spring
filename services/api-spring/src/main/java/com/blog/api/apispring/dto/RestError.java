package com.blog.api.apispring.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public class RestError {

	private final RestErrorDetails error;

	public RestError(int statusCode, String message) {
		this.error = new RestErrorDetails(statusCode, message);
	}

	public RestError(HttpStatus httpStatus, String message) {
		this(httpStatus.value(), message);
	}

	public static class RestErrorDetails {
		private int statusCode;
		private String message;

		public RestErrorDetails(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@JsonIgnore
		public int getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
	}

	public RestErrorDetails getError() {
		return error;
	}
}


