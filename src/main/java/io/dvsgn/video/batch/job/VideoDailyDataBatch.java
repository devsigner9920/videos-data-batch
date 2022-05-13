package io.dvsgn.video.batch.job;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.dvsgn.video.batch.VideoType;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

@Configuration
public class VideoDailyDataBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final List<Long> videoIds = new ArrayList<>();

    public VideoDailyDataBatch(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job test() {
        return jobBuilderFactory.get("test")
                .start(step1())
                .build();

    }

    private Step step1() {
        return stepBuilderFactory.get("testStep")
                .tasklet((contribution, chunkContext) -> {
                    getDailyVideoIds(null);
                    return RepeatStatus.FINISHED;
                })
//                .chunk(10)
//                .reader(reader())
                .build();
    }

    private ItemReader<?> reader() {
        return new JsonItemReaderBuilder()
                .name(VideoType.MOVIES.name())
                .build();
    }

    private void getDailyVideoIds(String date) {
        byte[] fileByteArray = readFileByteArray(date);

        try(InputStream inputStream = new ByteArrayInputStream(fileByteArray);
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
            StringBuilder sb = gzipToStringBuilder(gzipInputStream);

            List<Long> videoIds = stringJsonToVideoIds(sb.toString());

            System.out.println("videoTemps.size() = " + videoIds.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StringBuilder gzipToStringBuilder(GZIPInputStream gzipInputStream) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int len;
        while ((len = gzipInputStream.read(buffer)) >= 0) {
            String str = new String(buffer, 0, len, StandardCharsets.UTF_8);
            sb.append(str);
        }
        return sb;
    }

    private List<Long> stringJsonToVideoIds(String string) {
        List<Long> videoIds = new ArrayList<>();
        for (String s : string.split("\n")) {

            videoIds.add(new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
                    .fromJson(s, JsonObject.class)
                    .get("id")
                    .getAsLong());
//            videoTemps.add(new GsonBuilder()
//                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                    .create()
//                    .fromJson(s, VideoTemp.class));
        }
        return videoIds;
    }

    private byte[] readFileByteArray(String date) {
        return createFileReadWebClient()
                .get()
                .uri(getMovieExportUri(date))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    private WebClient createFileReadWebClient() {
        return WebClient.create()
                .mutate()
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(-1))
                .build();
    }

    private Function<UriBuilder, URI> getMovieExportUri(String date) {
        String exportDate = dateFormatting(date);

        return uriBuilder -> uriBuilder.scheme("https")
                .host("files.tmdb.org")
                .path("p/")
                .path("exports/")
                .path("movie_ids_" + exportDate + ".json.gz")
                .build();
    }

    private String dateFormatting(String date) {
        LocalDate localDate;
        if (Objects.isNull(date)) {
            localDate = LocalDate.now();
        } else {
            localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        }

        return localDate.format(DateTimeFormatter.ofPattern("MM_dd_yyyy"));
    }
}
