package spbsut.kovalev.NotificationBot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final static String BOT_NAME = "NotificationBot";
    private final static String BOT_TOKEN = System.getenv("BOT_TOKEN");

    public TelegramBot() {
        super(BOT_TOKEN);
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
}


