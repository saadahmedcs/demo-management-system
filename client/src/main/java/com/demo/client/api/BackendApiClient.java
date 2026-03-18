package com.demo.client.api;

import com.demo.client.api.dto.BookSlotRequest;
import com.demo.client.api.dto.CourseDto;
import com.demo.client.api.dto.CreateCourseRequest;
import com.demo.client.api.dto.DemoSlotDto;
import com.demo.client.api.dto.GenerateSlotsRequest;
import com.demo.client.api.dto.MessageDto;
import com.demo.client.api.dto.SendMessageRequest;
import com.demo.client.api.dto.TimetableEntryDto;
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

  public DemoSlotDto bookSlot(UUID courseId, UUID slotId, String studentEmail)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(
                baseUri.resolve("/api/courses/" + courseId + "/slots/" + slotId + "/book"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new BookSlotRequest(studentEmail))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), DemoSlotDto.class);
  }

  public DemoSlotDto unbookSlot(UUID courseId, UUID slotId, String studentEmail)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(
                baseUri.resolve("/api/courses/" + courseId + "/slots/" + slotId + "/unbook"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new BookSlotRequest(studentEmail))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), DemoSlotDto.class);
  }

  public List<MessageDto> listMessages(UUID courseId) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/messages"))
            .GET()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public MessageDto sendMessage(UUID courseId, String senderEmail, String text)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/messages"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new SendMessageRequest(senderEmail, text))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), MessageDto.class);
  }

  public record RegisterUserRequest(String email, String role) {}
  public record UserRoleDto(String email, String role) {}

  public UserRoleDto registerUser(String email, String role) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/users/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new RegisterUserRequest(email, role))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), UserRoleDto.class);
  }

  public List<TimetableEntryDto> listTimetable(String email) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/timetable/" + email)).GET().build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public List<TimetableEntryDto> saveTimetable(String email, List<TimetableEntryDto> entries)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/timetable/" + email))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(entries)))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  private void ensureOk(HttpResponse<String> res) {
    if (res.statusCode() >= 200 && res.statusCode() < 300) return;
    String body = res.body();
    try {
      var node = om.readTree(body);
      if (node.has("message")) body = node.get("message").asText();
    } catch (Exception ignored) {}
    throw new RuntimeException(body);
  }
}

