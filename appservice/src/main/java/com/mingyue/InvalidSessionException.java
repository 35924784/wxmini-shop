package com.mingyue;

import java.io.Serializable;

public class InvalidSessionException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    public InvalidSessionException() {
        super();
    }

    public InvalidSessionException(String msg) {
        super(msg);
    }

    public InvalidSessionException(String msg, Exception e) {
        super(msg, e);
    }
}