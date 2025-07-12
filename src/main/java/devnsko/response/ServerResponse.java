package devnsko.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerResponse(String message, Object body) {
    public static ServerResponse ok(Object body) {
        return new ServerResponse("Success", body);
    }

    public static ServerResponse ok() {
        return new ServerResponse("Success", null);
    }

    public static ServerResponse error(String message) {
        return new ServerResponse(message, null);
    }
}