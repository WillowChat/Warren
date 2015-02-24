package engineer.carrot.warren;

import javax.annotation.Nullable;

public enum AccessLevel {
    OP,
    HOP,
    VOICE,
    NONE;

    @Nullable
    public static AccessLevel parseFromIdentifier(char identifier) {
        // TODO: Check if server specific
        // TODO: Add more levels

        switch (identifier) {
            case CharacterCodes.AT:
                return OP;

            default:
                return null;
        }
    }
}
