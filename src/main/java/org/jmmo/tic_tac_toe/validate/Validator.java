package org.jmmo.tic_tac_toe.validate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Validator {

    public String validateName(String name) {
        if (!StringUtils.hasText(name)) {
            Error.BAD_NAME.fire();
        }

        return name;
    }
}
