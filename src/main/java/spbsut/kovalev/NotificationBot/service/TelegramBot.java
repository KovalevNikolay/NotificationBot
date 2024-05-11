package spbsut.kovalev.NotificationBot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import spbsut.kovalev.NotificationBot.entity.User;
import spbsut.kovalev.NotificationBot.repository.AdminRepository;
import spbsut.kovalev.NotificationBot.repository.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;
    private final static String BOT_NAME = "NotificationBot";
    private final static String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final static String FIRST_SILENCE_MODE = "firstSilenceMode";
    private final static String SECOND_SILENCE_MODE = "secondSilenceMode";
    private final static String THIRD_SILENCE_MODE = "thirdSilenceMode";
    private final static String FOURTH_SILENCE_MODE = "fourthSilenceMode";

    public TelegramBot() {
        super(BOT_TOKEN);
        initializeBotMenu();
    }

    private void initializeBotMenu() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Получить сообщение приветствия"));
        commands.add(new BotCommand("/setSilenceMode", "Настроить режим тишины"));
        commands.add(new BotCommand("/myData", "Посмотреть свои данные"));
        commands.add(new BotCommand("/deleteMyData", "Удалить свои данные"));
        commands.add(new BotCommand("/help", "Информация о том,как пользоваться ботом"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(STR."Ошибка при настройке списка команд бота: \{e.getMessage()}");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> {
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                }
                case "/setSilenceMode" -> {
                    setSilenceModeCommandReceived(chatId);
                }
                default -> sendMessage(chatId, "Извините, команда не была распознана");
            }
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty() && !adminRepository.findById(message.getChatId()).isEmpty()) {
            var chat = message.getChat();

            User user = new User();
            user.setChatId(message.getChatId());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setBio(chat.getBio());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            log.info(STR."Пользователь сохранен: \{user}");
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode(STR."Привет, \{firstName}, приятно познакомиться!:blush:");
        sendMessage(chatId, answer);
        log.info(STR."Бот ответил пользователю: \{chatId} \{firstName}");
    }

    private void setSilenceModeCommandReceived(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode("Настройте режим \"Тишины\" - промежуток времени, когда Вы не будете получать сообщения. \n:heavy_exclamation_mark:Время указано в соответствии с Московским временем (MSK)\n\nВыберите наиболее удобный режим: "));

        InlineKeyboardMarkup markup =  new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        List<InlineKeyboardButton> secondLine = new ArrayList<>();
        List<InlineKeyboardButton> thirdLine = new ArrayList<>();
        List<InlineKeyboardButton> fourthLine = new ArrayList<>();

        var firstSilenceModeButton = new InlineKeyboardButton();
        firstSilenceModeButton.setCallbackData(FIRST_SILENCE_MODE);
        firstSilenceModeButton.setText("с 6:00 по 12:00");
        firstLine.add(firstSilenceModeButton);

        var secondSilenceModeButton = new InlineKeyboardButton();
        secondSilenceModeButton.setCallbackData(SECOND_SILENCE_MODE);
        secondSilenceModeButton.setText("с 10:00 по 20:00");
        secondLine.add(secondSilenceModeButton);

        var thirdSilenceModeButton = new InlineKeyboardButton();
        thirdSilenceModeButton.setCallbackData(THIRD_SILENCE_MODE);
        thirdSilenceModeButton.setText("с 22:00 по 8:00");
        thirdLine.add(thirdSilenceModeButton);

        var fourthSilenceModeButton = new InlineKeyboardButton();
        fourthSilenceModeButton.setCallbackData(FOURTH_SILENCE_MODE);
        fourthSilenceModeButton.setText("Получать сообщения всегда!");
        fourthLine.add(fourthSilenceModeButton);

        rowsInLine.add(firstLine);
        rowsInLine.add(secondLine);
        rowsInLine.add(thirdLine);
        rowsInLine.add(fourthLine);
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);
        send(message);
    }


    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        send(message);
    }

    private void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(STR."Произошла ошибка: \{e.getMessage()}");
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
}


