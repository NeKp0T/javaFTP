package com.example.ftp.client;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListRequestAnswer {
    @NotNull
    public final List<FileDescription> files;
    @NotNull
    public final RequestResult status;

    public ListRequestAnswer(@NotNull List<FileDescription> files, @NotNull RequestResult status) {
        this.files = files;
        this.status = status;
    }

    public ListRequestAnswer(@NotNull RequestResult status) {
        this(new ArrayList<>(), status);
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        result.append("{status = ").append(status).append(", files = [");
        for (Iterator<FileDescription> iterator = files.iterator(); iterator.hasNext();) {
            result.append(iterator.next());
            if (iterator.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]}");
        return result.toString();
    }
}
