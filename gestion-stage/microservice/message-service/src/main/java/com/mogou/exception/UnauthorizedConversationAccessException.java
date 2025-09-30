package com.mogou.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Accès non autorisé à cette conversation")
public class UnauthorizedConversationAccessException extends RuntimeException {
    public UnauthorizedConversationAccessException(String message) {
        super(message);
    }
}