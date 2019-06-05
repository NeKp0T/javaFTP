package com.example.ftp.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class GetRequestAnswer {
    /**
     * Size of file, or <code>-1</code> if this file does not exist.
     */
    @NotNull
    public final long size;

    /**
     * File's content or null if file does not exist or request finished with any error.
     */
    @Nullable
    public final String contents;

    /**
     * Status of request.
     */
    @NotNull
    public final RequestStatus status;

    public GetRequestAnswer(@NotNull long size, @Nullable String contents, @NotNull RequestStatus status) {
        this.size = size;
        this.contents = contents;
        this.status = status;
    }

    public static GetRequestAnswer error() {
        return new GetRequestAnswer(0, null, RequestStatus.CRITICAL_ERROR);
    }

    @Override
    public String toString() {
        return "{status = " + status + ", size = " + size + ", contents:\n" + contents + "\n}";
    }
}
