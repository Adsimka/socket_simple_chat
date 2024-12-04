package constants;

public enum Message {

    ENTER_NICKNAME("Please enter a nickname: "),
    NICKNAME_IS_NOT_VALID("Nickname is not valid"),
    SUCCESSFULLY_CHANGE("Successfully changed nickname to %s"),
    RENAMED_THEMSELVES("%s renamed themselves to %s"),
    CONNECTED("%s connected!"),
    LEFT_THE_CHAT("%s left the chat!");


    private String description;

    Message(String description) {
        this.description = description;
    }

    public String getString(Object... params) {
        try {
            return String.format(this.description, params);
        } catch (Exception e) {
            return "Ошибка формирования log " + this;
        }
    }
}
