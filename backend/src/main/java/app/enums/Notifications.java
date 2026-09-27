package app.enums;


import lombok.Getter;

@Getter
public enum Notifications {

    // LOGIN
    LOGGED_IN("Welcome back %s!"),
    WRONG_CREDENTIALS("You entered the wrong credentials!"),
    PASSWORD_CHANGED("Password was changed successfully"),
    PASSWORD_RESET_REQUESTED("If the email exists, a password reset link has been sent"),
    PASSWORD_RESET_SUCCESS("Password has been reset successfully"),
    PASSWORD_CURRENT_MISSING("You must enter your current password"),
    PASSWORD_CURRENT_WRONG("Current password is wrong"),
    PASSWORD_NEW_MISSING("You must enter a new password"),
    PASSWORD_REPEAT_MISSING("You must repeat your new password"),
    PASSWORD_UNCHANGED("New password must be different from current password"),

    // VALIDATE PASSWORD
    PASSWORD_LENGTH_INVALID("The password must be between 8 and 30 characters"),
    PASSWORD_LOWERCASE_MISSING("The password must contain a lowercase letter"),
    PASSWORD_UPPERCASE_MISSING("The password must contain an uppercase letter"),
    PASSWORD_SPECIAL_CHAR_MISSING("The password must contain a special character"),


    // REGISTER
    EMAIL_EXISTS("Email: %s already exists! Choose another email"),
    REGISTER_SUCCESS("Welcome to Wedding planner %s! We hope you will enjoy the site"),
    REGISTER_NO_EMAIL("You must enter a valid email"),
    REGISTER_NO_PASSWORD("You must enter a valid password"),
    REGISTER_NO_PASSWORD_REPEAT("You must verify your password"),
    REGISTER_PASSWORD_MISMATCH("Password confirmation does not match"),
    REGISTER_NO_FIRSTNAME("You must enter your first name"),
    REGISTER_NO_LASTNAME("You must enter your last name"),
    TOKEN_MISSING("Token is missing"),
    LINK_EXPIRED("Link is expired or invalid"),
    EMAIL_CONFIRMED("Email has been confirmed"),
    EMAIL_CONFIRMATION_RESENT("If the email address belongs to an unverified account, we will send a verification email."),
    USER_NOT_FOUND("User not found."),
    USER_ID_INVALID("User id must be a valid UUID."),
    ADMIN_REQUIRED("Administrator access is required."),


    // WEDDING
    WEDDING_CREATED("Wedding created."),
    WEDDING_UPDATED("Wedding updated."),
    WEDDING_DELETED("Wedding deleted."),
    WEDDING_NOT_FOUND("Wedding not found."),
    WEDDING_LOGIN_REQUIRED("Authentication required."),
    WEDDING_ID_INVALID("Wedding id must be a valid UUID."),
    WEDDING_DATE_INVALID("date must be a valid date in YYYY-MM-DD format."),
    WEDDING_TITLE_REQUIRED("You must enter a wedding title."),
    WEDDING_DESCRIPTION_REQUIRED("You must enter a wedding description."),
    WEDDING_BUDGET_INVALID("budget must be a number."),
    WEDDING_BUDGET_DECIMALS("budget must have at most two decimal places."),
    WEDDING_BUDGET_RANGE("budget must not be negative."),
    WEDDING_FIELD_REQUIRED("%s is required."),
    WEDDING_FIELD_TEXT("%s must be a string."),
    WEDDING_FIELD_LENGTH("%s must be at most %s characters."),

    // CATEGORY
    CATEGORY_CREATED("Category created."),
    CATEGORY_UPDATED("Category updated."),
    CATEGORY_DELETED("Category deleted."),
    CATEGORY_NOT_FOUND("Category not found."),
    CATEGORY_GET_ALL("Categories fetched."),
    CATEGORY_ID_INVALID("Category id must be a valid UUID."),
    CATEGORY_TITLE_REQUIRED("You must enter a category title."),
    CATEGORY_UNCATEGORIZED_DELETE("The Uncategorized category cannot be deleted."),
    CATEGORY_NAME_RESERVED("Uncategorized is a reserved category name."),
    CATEGORY_UNCATEGORIZED_RENAME("The Uncategorized category cannot be renamed or replaced."),
    CATEGORY_BUDGET_INVALID("Category budget is invalid"),

    // TASK
    TASK_CREATED("Task created."),
    TASK_UPDATED("Task updated."),
    TASK_DELETED("Task deleted."),
    TASK_NOT_FOUND("Task not found."),
    TASK_GET_ALL("Tasks fetched."),
    TASK_ID_INVALID("Task id must be a valid UUID."),
    TASK_TITLE_REQUIRED("You must enter a task title."),
    TASK_DEADLINE_INVALID("Deadline must be a valid date in YYYY-MM-DD format."),
    TASK_PRIORITY_INVALID("Priority must be LOW, MEDIUM or HIGH."),
    TASK_STATUS_INVALID("Status must be TODO, IN_PROGRESS or DONE."),
    TASK_STATUS_UPDATED("Task status updated."),
    TASK_ESTIMATE_INVALID("Estimated hours must be a number."),
    TASK_MOVE_DIFFERENT_WEDDING("Tasks can only be moved between categories in the same wedding."),
    POSITION_INVALID("position must be an integer."),
    POSITION_RANGE("position must be between 0 and %s."),


    // GENERICS
    GET_ALL_EMPTY("No data was fetched because %s was empty!"),
    GET_BY_ID("You fetched %s with ID: %s"),
    GET_ALL("You fetched %s %ss"),
    BODY_EMPTY("Body is empty or invalid!"),
    MUST_BE_FLOAT("The entered must be a decimal number!")

    ;

    // ________________________________________________________

    private final String displayName;

    // ________________________________________________________

    Notifications(String displayName) {
        this.displayName = displayName;
    }
}
