package spbsut.kovalev.NotificationBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final static String BOT_NAME = "NotificationBot";
    private final static String BOT_TOKEN = System.getenv("BOT_TOKEN");

    public TelegramBot() {
        super(BOT_TOKEN);
        initializeBotMenu();
    }

    private void initializeBotMenu() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Получить сообщение приветствия"));
        commands.add(new BotCommand("/mydata", "Посмотреть свои данные"));
        commands.add(new BotCommand("/deletedata", "Удалить свои данные"));
        commands.add(new BotCommand("/help", "Информация о том,как пользоваться ботом"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(STR."Error settings bot's command list: \{e.getMessage()}");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText())  {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(chatId, "Извините, команда не была распознана");
                    break;
            }
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = STR."Привет, \{firstName}, приятно познакомиться!";
        sendMessage(chatId, answer);
        log.info(STR."Replied to user \{firstName}");
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(STR."Error occurred :\{e.getMessage()}");
        }
    }


    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
}


