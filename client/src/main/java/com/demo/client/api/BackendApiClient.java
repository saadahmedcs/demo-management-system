package com.demo.client.api;

import com.demo.client.api.dto.BookSlotRequest;
import com.demo.client.api.dto.CourseDto;
import com.demo.client.api.dto.CreateCourseRequest;
import com.demo.client.api.dto.DemoSlotDto;
import com.demo.client.api.dto.GenerateSlotsRequest;
import com.demo.client.api.dto.MarkDto;
import com.demo.client.api.dto.MessageDto;
import com.demo.client.api.dto.SendMessageRequest;
import com.demo.client.api.dto.TimetableEntryDto;
import java.util.Base64;
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

  public DemoSlotDto bookSlot(
      UUID courseId,
      UUID slotId,
      String studentEmail,
      Integer groupMemberCount,
      List<String> groupMemberRollNumbers)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(
                baseUri.resolve("/api/courses/" + courseId + "/slots/" + slotId + "/book"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(
                    new BookSlotRequest(studentEmail, groupMemberCount, groupMemberRollNumbers))))
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
                om.writeValueAsString(new BookSlotRequest(studentEmail, null, null))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), DemoSlotDto.class);
  }

  public List<MessageDto> listMessages(UUID courseId, String viewerEmail, String peerEmail)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(
                baseUri.resolve(
                    "/api/courses/"
                        + courseId
                        + "/messages?viewerEmail="
                        + java.net.URLEncoder.encode(viewerEmail, java.nio.charset.StandardCharsets.UTF_8)
                        + "&peerEmail="
                        + java.net.URLEncoder.encode(peerEmail, java.nio.charset.StandardCharsets.UTF_8)))
            .GET()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public MessageDto sendMessage(UUID courseId, String senderEmail, String recipientEmail, String text)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/messages"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new SendMessageRequest(senderEmail, recipientEmail, text))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), MessageDto.class);
  }

  public record UserRoleDto(String email, String role) {}
  public record LoginRequest(String email, String password, String role) {}
  public record CreateAccountRequest(
      String email, String name, String rollNumber, String section, String password, String confirmPassword) {}

  public void createAccount(
      String email, String name, String rollNumber, String section, String password, String confirmPassword)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/auth/create-account"))
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    om.writeValueAsString(
                        new CreateAccountRequest(email, name, rollNumber, section, password, confirmPassword))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
  }

  public UserRoleDto login(String email, String password, String role)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    om.writeValueAsString(new LoginRequest(email, password, role))))
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

  private record UpdateVenueRequest(String venue) {}
  private record UploadRubricRequest(String filename, String contentBase64) {}
  private record SaveMarkRequest(String studentEmail, Double score, String feedback) {}
  private record EnrollRequest(String studentEmail, String enrollmentCode) {}

  public CourseDto joinCourse(String studentEmail, String enrollmentCode) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/enroll"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new EnrollRequest(studentEmail, enrollmentCode))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), CourseDto.class);
  }

  public List<CourseDto> listEnrolledCourses(String studentEmail) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/students/" + studentEmail + "/courses"))
            .GET()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public CourseDto getCourse(UUID courseId) throws IOException, InterruptedException {
    var req = HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId)).GET().build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), CourseDto.class);
  }

  public CourseDto updateVenue(UUID courseId, String venue) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/venue"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(new UpdateVenueRequest(venue))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), CourseDto.class);
  }

  public CourseDto uploadRubric(UUID courseId, String filename, byte[] content)
      throws IOException, InterruptedException {
    var b64 = Base64.getEncoder().encodeToString(content);
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/rubric"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new UploadRubricRequest(filename, b64))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), CourseDto.class);
  }

  public byte[] downloadRubric(UUID courseId) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/rubric"))
            .GET()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
    if (res.statusCode() < 200 || res.statusCode() >= 300)
      throw new RuntimeException("No rubric available for this course.");
    return res.body();
  }

  public List<MarkDto> listMarks(UUID courseId) throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/marks"))
            .GET()
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), new TypeReference<>() {});
  }

  public MarkDto saveMark(UUID courseId, UUID slotId, String studentEmail, Double score, String feedback)
      throws IOException, InterruptedException {
    var req =
        HttpRequest.newBuilder(baseUri.resolve("/api/courses/" + courseId + "/marks/" + slotId))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(
                om.writeValueAsString(new SaveMarkRequest(studentEmail, score, feedback))))
            .build();
    var res = http.send(req, HttpResponse.BodyHandlers.ofString());
    ensureOk(res);
    return om.readValue(res.body(), MarkDto.class);
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

