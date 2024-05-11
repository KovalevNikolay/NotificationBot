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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import spbsut.kovalev.NotificationBot.entity.User;
import spbsut.kovalev.NotificationBot.repository.AdminRepository;
import spbsut.kovalev.NotificationBot.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalTime;
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

    private final static String SET_SILENCE_MODE = "Настроить режим \"Тишины\"";
    private final static String DELETE_MY_DATA = "Удалить мои данные";
    private final static String READ_MY_DATA = "Мои данные";
    private final static String FIRST_SILENCE_MODE = "firstSM";
    private final static String SECOND_SILENCE_MODE = "secondSM";
    private final static String THIRD_SILENCE_MODE = "thirdSM";
    private final static String FOURTH_SILENCE_MODE = "fourthSM";

    public TelegramBot() {
        super(BOT_TOKEN);
    }

    private void initializeBotMenu() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Получить сообщение приветствия"));
        commands.add(new BotCommand("/setSilenceMode", "Настроить режим тишины"));
        commands.add(new BotCommand("/readMyData", "Посмотреть свои данные"));
        commands.add(new BotCommand("/deleteMyData", "Удалить свои данные"));

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
                case "/setSilenceMode", SET_SILENCE_MODE -> setSilenceModeCommandReceived(chatId);

                case "/readMyData", READ_MY_DATA -> readUserData(chatId);

                case "/deleteMyData", DELETE_MY_DATA -> //deleteUserData(chatId);
                        sendMessage(chatId, "Данные удалены!");
                default -> sendMessage(chatId, "Извините, команда не была распознана");
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            switch (callBackData) {
                case FIRST_SILENCE_MODE, SECOND_SILENCE_MODE, THIRD_SILENCE_MODE, FOURTH_SILENCE_MODE ->
                        setSilenceModeForUser(chatId, callBackData);
            }

        }
    }

    private void setSilenceModeForUser(long chatId, String callBackData) {

        LocalTime startQuietTime = null;
        LocalTime endQuietTime = null;

        switch (callBackData) {
            case FIRST_SILENCE_MODE -> {
                startQuietTime = LocalTime.of(6, 0, 0);
                endQuietTime = LocalTime.of(12, 0, 0);
            }
            case SECOND_SILENCE_MODE -> {
                startQuietTime = LocalTime.of(10, 0, 0);
                endQuietTime = LocalTime.of(20, 0, 0);
            }
            case THIRD_SILENCE_MODE -> {
                startQuietTime = LocalTime.of(22, 0, 0);
                endQuietTime = LocalTime.of(8, 0, 0);
            }
        }

        if (userRepository.existsById(chatId)) {
            User user = userRepository.findById(chatId).get();
            user.setStartQuietTime(startQuietTime);
            user.setEndQuietTime(endQuietTime);
            userRepository.save(user);
        }
    }

    private void deleteUserData(long chatId) {
        if (userRepository.existsById(chatId)) {
            userRepository.deleteById(chatId);
        }
    }

    private void readUserData(long chatId) {
        if (userRepository.existsById(chatId)) {
            User user = userRepository.findById(chatId).get();
            String userData = STR."Мои данные\nИдентификатор: \{user.getChatId()}\nИмя: \{user.getFirstName()}\nФамилия: \{user.getLastName()}\nUsername: \{user.getUserName()}\nБИО: \{user.getBio()}\nГруппа: \{user.getGroupId()}\nНе беспокоить с \{user.getStartQuietTime()} по \{user.getEndQuietTime()}\nДата регистрации: \{user.getRegisteredAt()}";
            sendMessage(chatId, userData);
        }
    }

    private void registerUser(Message message) {
        if (!userRepository.existsById(message.getChatId()) && !adminRepository.existsById(message.getChatId())) {
            var chat = message.getChat();

            User user = new User();
            user.setChatId(message.getChatId());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setBio(chat.getBio());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            user.setGroupId(0);
            user.setStartQuietTime(null);
            user.setEndQuietTime(null);

            userRepository.save(user);

            log.info(STR."Пользователь сохранен: \{user}");
        }
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode(STR."Привет, \{firstName}, приятно познакомиться!:blush:");
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(answer);

        if (userRepository.existsById(chatId)) {
            message.setReplyMarkup(getUserMenuKeyboardMarkup());
        } else if (adminRepository.existsById(chatId)){

        }
        send(message);
    }

    private void setSilenceModeCommandReceived(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode("Настройте режим \"Тишины\" - промежуток времени, когда Вы не будете получать сообщения от бота.\n\n:heavy_exclamation_mark:Время указано в соответствии с Московским временем (MSK).\n\nВыберите наиболее удобный режим: "));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
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

    private ReplyKeyboardMarkup getUserMenuKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(EmojiParser.parseToUnicode(SET_SILENCE_MODE));
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(EmojiParser.parseToUnicode(READ_MY_DATA));
        row.add(EmojiParser.parseToUnicode(DELETE_MY_DATA));
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        send(message);
        log.info(STR."Бот ответил пользователю: \{chatId}");
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


