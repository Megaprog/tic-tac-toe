package org.jmmo.tic_tac_toe.validate;

import org.javatuples.Pair;
import org.jmmo.tic_tac_toe.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Validator {

    @Autowired
    GameService gameService;

    public String validateName(String name) {
        if (!StringUtils.hasText(name)) {
            Error.BAD_NAME.fire();
        }

        return name;
    }

    public Pair<Integer, Integer> validateMove(Pair<Integer, Integer> move) {
        if (move.getValue0() == null || move.getValue1() == null
                || move.getValue0() < 0 || move.getValue0() > gameService.getMaxX() || move.getValue1() < 0 || move.getValue1() > gameService.getMaxY()) {
            Error.OUT_OF_BOARD.fire();
        }

        return move;
    }
}
