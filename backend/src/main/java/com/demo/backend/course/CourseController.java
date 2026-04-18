package com.demo.backend.course;

import com.demo.backend.course.dto.CourseDto;
import jakarta.validation.constraints.NotBlank;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
@Validated
public class CourseController {
  private final CourseService service;

  public record CreateCourseRequest(String code, @NotBlank String name, String taEmail, String enrollmentCode) {}
  public record UpdateVenueRequest(String venue) {}
  public record UploadRubricRequest(String filename, String contentBase64) {}

  @GetMapping
  public List<CourseDto> list() {
    return service.list();
  }

  @GetMapping("/{courseId}")
  public CourseDto get(@PathVariable UUID courseId) {
    return service.get(courseId);
  }

  @PostMapping
  public CourseDto create(@RequestBody CreateCourseRequest req) {
    if (req == null) throw new IllegalArgumentException("Missing request body.");
    return service.create(req.code(), req.name(), req.taEmail(), req.enrollmentCode());
  }

  @PutMapping("/{courseId}/venue")
  public CourseDto updateVenue(@PathVariable UUID courseId, @RequestBody UpdateVenueRequest req) {
    return service.updateVenue(courseId, req.venue());
  }

  @PutMapping("/{courseId}/rubric")
  public CourseDto uploadRubric(@PathVariable UUID courseId, @RequestBody UploadRubricRequest req) {
    if (req.filename() == null || req.contentBase64() == null)
      throw new IllegalArgumentException("filename and contentBase64 are required.");
    byte[] content = Base64.getDecoder().decode(req.contentBase64());
    service.uploadRubric(courseId, req.filename(), content);
    return service.get(courseId);
  }

  @GetMapping("/{courseId}/rubric")
  public ResponseEntity<byte[]> getRubric(@PathVariable UUID courseId) {
    var dto = service.get(courseId);
    var content = service.getRubricContent(courseId);
    var filename = dto.rubricFilename() != null ? dto.rubricFilename() : "rubric";
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(content);
  }
}
