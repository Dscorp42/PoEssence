package ua.dscorp.poessence.util;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import ua.dscorp.poessence.Application;

public class SoundPlayer {

    public static void playSound() {
        new Thread(() -> {
            try {
                Player playMP3 = new Player(Application.class.getResourceAsStream("/ua/dscorp/poessence/notification.mp3"));
                playMP3.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        }).start();
    }
}