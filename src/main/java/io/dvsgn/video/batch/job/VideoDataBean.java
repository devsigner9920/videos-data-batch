package io.dvsgn.video.batch.job;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VideoDataBean<T> {
    private final Map<String, T> data;

    private VideoDataBean() {
        this.data = new ConcurrentHashMap<>();
    }


}
