package app.enums;

import lombok.Getter;

@Getter
public enum Status {

    TODO("To-do"),
    IN_PROGRESS("In progress"),
    DONE("Done");

    // ________________________________________________________

    private final String displayName;

    // ________________________________________________________

    Status(String displayName) {
        this.displayName = displayName;
    }
}