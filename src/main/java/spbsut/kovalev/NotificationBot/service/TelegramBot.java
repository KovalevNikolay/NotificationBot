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
    private final static String FIRST_MODE = "firstSM";
    private final static String SECOND_MODE = "secondSM";
    private final static String THIRD_MODE = "thirdSM";
    private final static String FOURTH_MODE = "fourthSM";
    private final static String READ_USER_BUTTON = "READ_USER_BUTTON";
    private final static String USER_GROUPS = "Группы пользователей";
    private final static String ALL_USERS = "Все пользователи";
    private final static String APPOINT_AN_ADMIN = "Назначить администратора";
    private final static String READ_USER_DATA = "Показать данные пользователя";
    private final static String ADD_USER_TO_GROUP = "Добавить пользователя в группу";
    private final static String REMOVE_USER_FROM_GROUP = "Удалить пользователя из группы";
    private final static String SHOW_GROUP_USERS = "Показать пользователей";
    private final static String CREATE_GROUP = "Создать группу";
    private final static String EDIT_GROUP = "Изменить группу";
    private final static String REMOVE_GROUP = "Удалить группу";
    private final static String SEND_MESSAGE = "Отправить сообщение";
    private final static String CREATE_TEMPLATE = "Создать шаблон сообщения";
    private final static String MESSAGE_HISTORY = "История сообщений";
    private final static String BACK_TO_MAIN_MENU = "В главное меню";


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

            if (adminRepository.existsById(chatId)) {
                switch (messageText) {
                }
            } else {
                switch (messageText) {
                    case "/start" -> {
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        registerUser(update.getMessage());
                    }
                    case "/setSilenceMode", SET_SILENCE_MODE -> setSilenceModeCommandReceived(chatId);
                    case "/readMyData", READ_MY_DATA -> readUserData(chatId, chatId);
                    case "/deleteMyData", DELETE_MY_DATA -> {
                        deleteUserData(chatId);
                        sendMessage(chatId, EmojiParser.parseToUnicode(":x:Данные удалены!:x:"));
                    }

                    case USER_GROUPS -> userGroupCommandRecieved(chatId);
                    case ALL_USERS ->
                            showUsers(chatId, 0, ":man_technologist:Список всех пользователей:", READ_USER_BUTTON);
                    case BACK_TO_MAIN_MENU -> mainMenu(chatId);

                    default ->
                            sendMessage(chatId, EmojiParser.parseToUnicode(":warning:Извините, команда не была распознана!:warning:"));
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();


            if (callBackData.equals(FIRST_MODE) || callBackData.equals(SECOND_MODE) || callBackData.equals(THIRD_MODE)) {
                setSilenceModeForUser(chatId, callBackData);
            } else if (callBackData.contains(READ_USER_BUTTON)) {
                long userId = Long.valueOf(callBackData.substring(callBackData.indexOf(" ") + 1));
                readUserData(chatId, userId);
            }

        }
    }

    private void mainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Главное меню");
        message.setReplyMarkup(getAdminKeyboardMarkup());
        send(message);
    }

    private void showUsers(long chatId, int groupId, String messageText, String callbackData) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(messageText));

        Iterable<User> users = groupId > 0 ? userRepository.findByGroupId(groupId) : userRepository.findAll();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        for (User user : users) {
            List<InlineKeyboardButton> line = new ArrayList<>();
            var userButton = new InlineKeyboardButton();
            userButton.setText(STR."\{user.getChatId()} \{user.getUserName()}");
            userButton.setCallbackData(STR."\{callbackData} \{user.getChatId()}");
            line.add(userButton);
            rowsInLine.add(line);
        }
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);
        send(message);
    }

    private void userGroupCommandRecieved(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы перешли в меню управления группами пользователей!");
        message.setReplyMarkup(getGroupMenu());
        send(message);
    }

    private void setSilenceModeForUser(long chatId, String callBackData) {

        LocalTime startQuietTime = null;
        LocalTime endQuietTime = null;

        switch (callBackData) {
            case FIRST_MODE -> {
                startQuietTime = LocalTime.of(6, 0, 0);
                endQuietTime = LocalTime.of(12, 0, 0);
            }
            case SECOND_MODE -> {
                startQuietTime = LocalTime.of(10, 0, 0);
                endQuietTime = LocalTime.of(20, 0, 0);
            }
            case THIRD_MODE -> {
                startQuietTime = LocalTime.of(22, 0, 0);
                endQuietTime = LocalTime.of(8, 0, 0);
            }
        }

        if (userRepository.findById(chatId).isPresent()) {
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

    private void readUserData(long chatId, long userDataId) {
        if (userRepository.findById(userDataId).isPresent()) {
            User user = userRepository.findById(userDataId).get();
            String userData = STR."Идентификатор: \{user.getChatId()}\nИмя: \{user.getFirstName()}\nФамилия: \{user.getLastName()}\nUsername: \{user.getUserName()}\nБИО: \{user.getBio()}\nГруппа: \{user.getGroupId()}\nНе беспокоить с \{user.getStartQuietTime()} по \{user.getEndQuietTime()}\nДата регистрации: \{user.getRegisteredAt()}";
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
            user.setGroupId(null);
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
//        uncomment
//        if (userRepository.existsById(chatId)) {
//            message.setReplyMarkup(getUserKeyboardMarkup());
//        } else if (adminRepository.existsById(chatId)) {
//            message.setReplyMarkup(getAdminKeyboardMarkup());
//        }
        message.setReplyMarkup(getAdminKeyboardMarkup()); // delete
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
        firstSilenceModeButton.setCallbackData(FIRST_MODE);
        firstSilenceModeButton.setText("с 6:00 по 12:00");
        firstLine.add(firstSilenceModeButton);

        var secondSilenceModeButton = new InlineKeyboardButton();
        secondSilenceModeButton.setCallbackData(SECOND_MODE);
        secondSilenceModeButton.setText("с 10:00 по 20:00");
        secondLine.add(secondSilenceModeButton);

        var thirdSilenceModeButton = new InlineKeyboardButton();
        thirdSilenceModeButton.setCallbackData(THIRD_MODE);
        thirdSilenceModeButton.setText("с 22:00 по 8:00");
        thirdLine.add(thirdSilenceModeButton);

        var fourthSilenceModeButton = new InlineKeyboardButton();
        fourthSilenceModeButton.setCallbackData(FOURTH_MODE);
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

    private ReplyKeyboardMarkup getUserKeyboardMarkup() {
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

    private ReplyKeyboardMarkup getAdminKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(CREATE_TEMPLATE);
        row.add(SEND_MESSAGE);
        row.add(MESSAGE_HISTORY);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(ALL_USERS);
        row.add(USER_GROUPS);
        row.add(APPOINT_AN_ADMIN);
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getGroupMenu() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(CREATE_GROUP);
        row.add(EDIT_GROUP);
        row.add(REMOVE_GROUP);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(ADD_USER_TO_GROUP);
        row.add(SHOW_GROUP_USERS);
        row.add(REMOVE_USER_FROM_GROUP);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(BACK_TO_MAIN_MENU);
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