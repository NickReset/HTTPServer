package social.nickrest.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BooleanUtil {

    public boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    public boolean valueOf(String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return false;
        }
    }
}
