package com.demo.client.api;

import com.demo.client.api.dto.CourseDto;
import com.demo.client.api.dto.CreateCourseRequest;
import com.demo.client.api.dto.DemoSlotDto;
import com.demo.client.api.dto.GenerateSlotsRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class BackendApiClient {
  private final URI baseUri;
  private final HttpClient http = HttpClient.newHttpClient();
  private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

  public BackendApiClient(URI baseUri) {
    this.baseUri = baseUri;
  }

  public List<CourseDto> listCourses() throws IOException, InterruptedException {
    var req = HttpRequest.newBuilder(baseUri.resolve("/api/courses")).GET().build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public CourseDto createCourse(CreateCourseRequest body) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body)))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), CourseDto.class);
  }

  public List<DemoSlotDto> listSlots(UUID courseId) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/slots"))
            .GET()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public List<DemoSlotDto> generateSlots(UUID courseId, GenerateSlotsRequest body)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/slots/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body)))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public void clearSlots(UUID courseId) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/slots"))
            .DELETE()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
  }

  private static void ensureOk(HttpResponse<String> res) {
    if (res.statusCode() >= 200 && res.statusCode() < 300) return;
    throw new RuntimeException("Backend error (" + res.statusCode() + "): " + res.body());
  }
}

