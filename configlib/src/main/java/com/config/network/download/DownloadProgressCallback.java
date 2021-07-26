package com.config.network.download;

public interface DownloadProgressCallback {
    void update(long bytesRead, long contentLength, boolean done);
}
