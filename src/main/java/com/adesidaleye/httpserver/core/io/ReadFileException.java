package com.adesidaleye.httpserver.core.io;

import java.io.IOException;

public class ReadFileException extends Exception {
    public ReadFileException(IOException e) {
        super(e);
    }

    public ReadFileException(String message) {
        super(message);
    }

    public ReadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
