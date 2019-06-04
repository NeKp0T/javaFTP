package com.example.ftp.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class GetRequestAnswer {
    @NotNull
    public final long size;
    @Nullable
    public final String contents;
    @NotNull
    public final RequestStatus status;

    public GetRequestAnswer(@NotNull long size, @Nullable String contents, @NotNull RequestStatus status) {
        this.size = size;
        this.contents = contents;
        this.status = status;
    }

    @Override
    public String toString() {
        return "{status = " + status + ", size = " + size + ", contents:\n" + contents + "\n}";
    }
}
