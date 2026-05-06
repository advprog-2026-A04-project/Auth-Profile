package id.ac.ui.cs.advprog.auth_profile.controller;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRequestURI("/auth/register");
    }

    @Test
    void handleResponseStatusShouldMirrorStatusAndReason() {
        var response = handler.handleResponseStatus(new ResponseStatusException(HttpStatus.CONFLICT, "duplicate"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("duplicate", response.getBody().message());
        assertEquals("/auth/register", response.getBody().path());
    }

    @Test
    void handleValidationShouldUseFirstFieldMessage() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "email", "Email is required."));
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                result
        );

        var response = handler.handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Email is required.", response.getBody().message());
        assertEquals("/auth/register", response.getBody().path());
    }

    @Test
    void handleValidationShouldUseFallbackMessageWhenNoFieldErrorExists() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                result
        );

        var response = handler.handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request validation failed.", response.getBody().message());
    }

    @Test
    void handleConstraintViolationShouldReturnBadRequest() {
        var response = handler.handleConstraintViolation(new ConstraintViolationException("bad request", Set.of()), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("bad request", response.getBody().message());
        assertEquals("/auth/register", response.getBody().path());
    }

    @SuppressWarnings("unused")
    private static final class SampleController {
        public void sample(@ModelAttribute String payload) {
        }
    }
}
