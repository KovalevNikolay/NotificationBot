package spbsut.kovalev.NotificationBot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import spbsut.kovalev.NotificationBot.entity.Administrator;
import spbsut.kovalev.NotificationBot.entity.Group;
import spbsut.kovalev.NotificationBot.entity.MessageTemplate;
import spbsut.kovalev.NotificationBot.entity.User;
import spbsut.kovalev.NotificationBot.repository.*;

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
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private MessageRepository messageRepository;
    private final static String BOT_NAME = "NotificationBot";
    private final static String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private final static String START_CMD = "/start";
    private final static String CREATE_GROUP_CMD = "/createGroup";
    private final static String SET_SILENCE_MODE = "Настроить режим \"Тишины\"";
    private final static String DELETE_MY_DATA = "Удалить мои данные";
    private final static String READ_MY_DATA = "Мои данные";
    private final static String FIRST_S_M = "FIRST_SILENCE_MODE";
    private final static String SECOND_S_M = "SECOND_SILENCE_MODE";
    private final static String THIRD_S_M = "THIRD_SILENCE_MODE";
    private final static String FOURTH_S_M = "FOURTH_SILENCE_MODE";
    private final static String READ_USER_BUTTON = "READ_USER_BUTTON";
    private final static String DELETE_USER_BUTTON = "DELETE_USER_BUTTON";
    private final static String ADD_USER_BUTTON = "ADD_USER_BUTTON";
    private final static String MAKE_AN_ADMIN = "MAKE_AN_ADMIN";
    private final static String USER_GROUPS = "Группы пользователей";
    private final static String ALL_USERS = "Все пользователи";
    private final static String APPOINT_AN_ADMIN = "Назначить администратора";
    private final static String ADD_USER_TO_GROUP = "Добавить пользователя в группу";
    private final static String DELETE_USER_FROM_GROUP = "Удалить пользователя из группы";
    private final static String READ_USERS_FROM_GROUP = "Показать пользователей";
    private final static String LIST_GROUP_USERS = ":sparkle:Список пользователей группы:sparkle:";
    private final static String CREATE_GROUP = "Создать группу";
    private final static String EDIT_GROUP_NAME = "Изменить группу";
    private final static String DELETE_GROUP = "Удалить группу";
    private final static String SEND_MESSAGE = "Отправить сообщение";
    private static final String CREATE_MESSAGE = "Новое сообщение";
    private static final String USE_TEMPLATE_MESSAGE = "Использовать шаблон";
    private final static String CREATE_TEMPLATE = "Создать шаблон сообщения";
    private final static String CREATE_TEMPLATE_CMD = "/createTemplate";
    private static final String TEMPLATE_BUTTON = "TEMPLATE_BUTTON";
    private static final String CHANGE_GROUP_NAME_CMD = "/changeGroupName";
    private final static String MESSAGE_HISTORY = "История сообщений";
    private final static String BACK_TO_MAIN_MENU = "В главное меню";
    private final static String COMMAND_NOT_RECOGNIZED = ":warning:Извините, команда не была распознана!:warning:";
    private final static String LIST_ALL_USERS = ":man_technologist:Список всех пользователей:";
    private static final String SEND_CMD = "/send";
    private static final String SEND_MESSAGE_INFO = STR."Для того, чтобы отправить сообщение введите команду \{SEND_CMD} после которой текст, который Вы хотите отправить.\n\nНапример:\n\{SEND_CMD} Hello World!";
    private final static String CREATE_GROUP_INFO = STR."Для того, чтобы создать новую группу введите команду \{CREATE_GROUP_CMD} и название группы.\n\nНапример:\n\{CREATE_GROUP_CMD} Моя Группа .";
    private static final String CHANGE_GROUP_NAME_INFO = STR."Для того, чтобы изменить название группы введите команду \{CHANGE_GROUP_NAME_CMD} и новое название для группы. \n\nНапример:\n\{CHANGE_GROUP_NAME_CMD} Новое название.";
    private final static String CREATE_TEMPLATE_INFO = STR."Для того, чтобы создать шаблон сообщения введите команду \{CREATE_TEMPLATE_CMD} и текст сообщения.\n\nНапример:\n\{CREATE_TEMPLATE_CMD} текст сообщения.";
    private final static String DATA_IS_DELETED = ":x:Данные удалены!:x:";
    private final static String THIS_NAME_ALREADY_EXISTS = ":x:Группа с таким названием уже существует!:x:";
    private final static String TEMPLATE_CREATED = ":heavy_check_mark: Шаблон сообщения создан!";
    private final static String TEMPLATE_WAS_NOT_CREATED = ":x:Шаблон с таким сообщением уже существует!:x:";
    private final static String SELECT_GROUP_FOR_READ = ":point_right:Выберите группу, пользователей которой необходимо показать:point_left:";
    private final static String SELECT_GROUP_FOR_DELETE = ":point_right:Выберите группу, из которой хотите удалить пользователя:point_left:";
    private final static String SELECT_GROUP_FOR_ADD = ":point_right:Выберите группу, в которую хотите добавить пользователя:point_left:";
    private final static String SELECT_GROUP_ON_DELETE = ":point_right:Выберите группу, которую хотите удалить:point_left:";
    private final static String SELECT_GROUP_FOR_SENDING = ":point_right:Выберите группу, которой хотите отправить сообщение:point_left:";
    private final static String SELECT_GROUP_FOR_CHANGE = ":point_right:Выберите группу, название которой хотите изменить:point_left:";
    private final static String SELECT_TEMPLATE = ":point_right:Выберите шаблон сообщения, который хотите использовать:point_left:";
    private static final String CHANGED_SUCCESSFULLY = "Название группы изменено на ";
    private static final String FOR_SENDING = "FOR_SENDING";
    private Integer idGroup;
    private String messageForSending;

    public TelegramBot() {
        super(BOT_TOKEN);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (adminRepository.existsById(chatId)) {
                if (messageText.equals(START_CMD)) {
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                } else if (messageText.equals(USER_GROUPS)) {
                    userGroupCommandRecieved(chatId);
                } else if (messageText.equals(ALL_USERS)) {
                    showUsers(chatId, -1, LIST_ALL_USERS, READ_USER_BUTTON);
                } else if (messageText.equals(BACK_TO_MAIN_MENU)) {
                    mainMenu(chatId);
                } else if (messageText.equals(APPOINT_AN_ADMIN)) {
                    showUsers(chatId, -1, LIST_ALL_USERS, MAKE_AN_ADMIN);
                } else if (messageText.equals(CREATE_GROUP)) {
                    sendMessage(chatId, CREATE_GROUP_INFO);
                } else if (messageText.equals(CREATE_TEMPLATE)) {
                    sendMessage(chatId, CREATE_TEMPLATE_INFO);
                } else if (messageText.contains(CREATE_GROUP_CMD)) {
                    String groupName = parseTextFromMessage(messageText);
                    String resultMessage = createGroup(groupName) ? STR.":heavy_check_mark: Группа \{groupName} создана!" : THIS_NAME_ALREADY_EXISTS;
                    sendMessage(chatId, resultMessage);
                } else if (messageText.contains(CREATE_TEMPLATE_CMD)) {
                    String templateMessage = parseTextFromMessage(messageText);
                    String resultMessage = createTemplateMessage(templateMessage) ? TEMPLATE_CREATED : TEMPLATE_WAS_NOT_CREATED;
                    sendMessage(chatId, resultMessage);
                } else if (messageText.contains(CHANGE_GROUP_NAME_CMD)) {
                    String newGroupName = parseTextFromMessage(CHANGE_GROUP_NAME_INFO);
                    String result = changeGroupName(newGroupName) ? CHANGED_SUCCESSFULLY + newGroupName : THIS_NAME_ALREADY_EXISTS;
                    sendMessage(chatId, result);
                } else if (messageText.contains(SEND_CMD)) {
                    messageForSending = parseTextFromMessage(SEND_CMD);
                    showGroups(chatId, SELECT_GROUP_FOR_SENDING, FOR_SENDING);
                } else if (messageText.equals(EDIT_GROUP_NAME)) {
                    showGroups(chatId, SELECT_GROUP_FOR_CHANGE, EDIT_GROUP_NAME);
                } else if (messageText.equals(READ_USERS_FROM_GROUP)) {
                    showGroups(chatId, SELECT_GROUP_FOR_READ, READ_USERS_FROM_GROUP);
                } else if (messageText.equals(DELETE_GROUP)) {
                    showGroups(chatId, SELECT_GROUP_ON_DELETE, DELETE_GROUP);
                } else if (messageText.equals(ADD_USER_TO_GROUP)) {
                    showGroups(chatId, SELECT_GROUP_FOR_ADD, ADD_USER_TO_GROUP);
                } else if (messageText.equals(DELETE_USER_FROM_GROUP)) {
                    showGroups(chatId, SELECT_GROUP_FOR_DELETE, DELETE_USER_FROM_GROUP);
                } else if (messageText.equals(SEND_MESSAGE)) {
                    sendMessageMenu(chatId);
                } else if (messageText.equals(USE_TEMPLATE_MESSAGE)) {
                    showTemplates(chatId);
                } else if (messageText.equals(CREATE_MESSAGE)) {
                    sendMessage(chatId, SEND_MESSAGE_INFO);
                } else {
                    sendMessage(chatId, COMMAND_NOT_RECOGNIZED);
                }
            } else {
                switch (messageText) {
                    case START_CMD -> {
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        registerUser(update.getMessage());
                    }
                    case SET_SILENCE_MODE -> setSilenceModeCommandReceived(chatId);
                    case READ_MY_DATA -> readUserData(chatId, chatId);
                    case DELETE_MY_DATA -> {
                        deleteUserData(chatId);
                        sendMessage(chatId, DATA_IS_DELETED);
                    }
                    default -> sendMessage(chatId, COMMAND_NOT_RECOGNIZED);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(FIRST_S_M) || callBackData.equals(SECOND_S_M) || callBackData.equals(THIRD_S_M) || callBackData.equals(FOURTH_S_M)) {
                setSilenceModeForUser(chatId, callBackData);
            } else if (callBackData.contains(READ_USER_BUTTON)) {
                long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
                readUserData(chatId, selectedChatId);
            } else if (callBackData.contains(MAKE_AN_ADMIN)) {
                long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
                appointAnAdministrator(selectedChatId);
                sendMessage(chatId, STR.":heavy_check_mark:Пользователь \{selectedChatId} назначен администратором!");
            } else if (callBackData.contains(READ_USERS_FROM_GROUP)) {
                int selectedGroupId = Integer.parseInt(getIdFromCallBackData(callBackData));
                showUsers(chatId, selectedGroupId, LIST_GROUP_USERS, READ_USER_BUTTON);
            } else if (callBackData.contains(DELETE_USER_FROM_GROUP)) {
                idGroup = Integer.parseInt(getIdFromCallBackData(callBackData));
                showUsers(chatId, idGroup, LIST_GROUP_USERS, DELETE_USER_BUTTON);
            } else if (callBackData.contains(DELETE_USER_BUTTON)) {
                long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
                deleteUserFromGroup(selectedChatId);
                changeCountUsersInGroup(-1);
                sendMessage(chatId, STR."Пользователь \{selectedChatId} удален из группы");
            } else if (callBackData.equals(ADD_USER_TO_GROUP)) {
                idGroup = Integer.parseInt(getIdFromCallBackData(callBackData));
                showUsers(chatId, 0, LIST_GROUP_USERS, ADD_USER_BUTTON);
            } else if (callBackData.contains(ADD_USER_BUTTON)) {
                long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
                addUserToGroup(selectedChatId);
                changeCountUsersInGroup(1);
                sendMessage(chatId, STR."Пользователь \{selectedChatId} добавлен в группу");
            } else if (callBackData.contains(DELETE_GROUP)) {
                int selectedGroupId = Integer.parseInt(getIdFromCallBackData(callBackData));
                deleteGroup(selectedGroupId);
            } else if (callBackData.contains(EDIT_GROUP_NAME)) {
                idGroup = Integer.parseInt(getIdFromCallBackData(callBackData));
                sendMessage(chatId, CHANGE_GROUP_NAME_INFO);
            } else if (callBackData.contains(TEMPLATE_BUTTON)) {
                int templateId = Integer.parseInt(getIdFromCallBackData(TEMPLATE_BUTTON));
                getMessageTextFromTemplate(templateId);
                showGroups(chatId, SELECT_GROUP_FOR_SENDING, FOR_SENDING);
            } else if (callBackData.contains(FOR_SENDING)) {
                int selectedGroupId = Integer.parseInt(getIdFromCallBackData(callBackData));
                sendMessageToUsersOfGroup(selectedGroupId);
                writeToTheMessageHistory(chatId);
            }
        }
    }

    private void sendMessageToUsersOfGroup(int selectedGroupId) {
        var users = userRepository.findByGroupId(selectedGroupId);
        LocalTime currentTime = LocalTime.now();
        for (var user : users) {
            if (currentTime.isAfter(user.getStartQuietTime()) && currentTime.isBefore(user.getEndQuietTime())) {
                //add to Queue<DataType>, DataType(User u, String text)
                //compare user.getStartQuietTime and user.getEndQuietTime
                //save to db
            } else {
                sendMessage(user.getChatId(), messageForSending);
            }
        }
    }

    private void writeToTheMessageHistory(final long chatId) {
        spbsut.kovalev.NotificationBot.entity.Message message = new spbsut.kovalev.NotificationBot.entity.Message();
        message.setMessageText(messageForSending);
        message.setSenderId(chatId);
        message.setTimeSending(new Timestamp(System.currentTimeMillis()));
        messageRepository.save(message);
    }

    private void showTemplates(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(SELECT_TEMPLATE));

        var templates = templateRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();

        for (var template : templates) {
            var line = new ArrayList<InlineKeyboardButton>();
            var templateButton = new InlineKeyboardButton();
            String text = template.getMessageText();
            text = text.length() > 30 ? text.substring(0, 30) + "..." : text;
            templateButton.setText(text);
            templateButton.setCallbackData(STR."\{TEMPLATE_BUTTON} \{template.getTemplateId()}");
            line.add(templateButton);
            rowsInLine.add(line);
        }
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);
        send(message);
    }

    private void getMessageTextFromTemplate(final int templateId) {
        if (templateRepository.findById(templateId).isPresent()) {
            var template = templateRepository.findById(templateId).get();
            messageForSending = template.getMessageText();
        }
    }

    private boolean changeGroupName(String newGroupName) {
        if (groupRepository.findById(idGroup).isPresent() && groupRepository.findByGroupName(newGroupName).isEmpty()) {
            Group group = groupRepository.findById(idGroup).get();
            group.setGroupName(newGroupName);
            groupRepository.save(group);
            return true;
        }
        return false;
    }

    private boolean createTemplateMessage(String templateMessage) {
        if (templateRepository.findByMessageText(templateMessage).isEmpty()) {
            MessageTemplate template = new MessageTemplate();
            template.setMessageText(templateMessage);
            templateRepository.save(template);
            return true;
        }
        return false;
    }

    private void deleteGroup(final int selectedGroupId) {
        if (groupRepository.findById(selectedGroupId).isPresent()) {
            var users = userRepository.findByGroupId(selectedGroupId);
            deleteAllUsersFromGroup(users);
            groupRepository.deleteById(selectedGroupId);
        }
    }

    private void deleteAllUsersFromGroup(Iterable<User> users) {
        for (var user : users) {
            user.setGroupId(0);
        }
        userRepository.saveAll(users);
    }

    private void deleteUserFromGroup(final long selectedChatId) {
        if (userRepository.findById(selectedChatId).isPresent()) {
            User user = userRepository.findById(selectedChatId).get();
            user.setGroupId(0);
            userRepository.save(user);
        }
    }

    private void changeCountUsersInGroup(final int value) {
        if (groupRepository.findById(idGroup).isPresent()) {
            Group group = groupRepository.findById(idGroup).get();
            group.setCountUsers(group.getCountUsers() + value);
            groupRepository.save(group);
            idGroup = 0;
        }
    }

    private void addUserToGroup(final long selectedChatId) {
        if (userRepository.findById(selectedChatId).isPresent()) {
            User user = userRepository.findById(selectedChatId).get();
            user.setGroupId(idGroup);
            userRepository.save(user);
        }
    }

    private boolean createGroup(String groupName) {
        if (groupRepository.findByGroupName(groupName).isEmpty()) {
            Group group = new Group();
            group.setGroupName(groupName);
            group.setCountUsers(0);
            groupRepository.save(group);
            return true;
        }
        return false;
    }

    private String parseTextFromMessage(String messageText) {
        return messageText.substring(messageText.indexOf(" ") + 1);
    }

    private void appointAnAdministrator(long selectedChatId) {
        if (userRepository.findById(selectedChatId).isPresent() && !adminRepository.existsById(selectedChatId)) {
            User user = userRepository.findById(selectedChatId).get();
            Administrator admin = new Administrator();

            admin.setChatId(user.getChatId());
            admin.setFirstName(user.getFirstName());
            admin.setLastName(user.getLastName());
            admin.setUserName(user.getUserName());
            admin.setBio(user.getBio());

            adminRepository.save(admin);
            userRepository.deleteById(selectedChatId);

            log.info(STR."Добавлен администратор: \{admin}");
            log.info(STR."Пользователь удален: \{user}");
        }
    }

    private String getIdFromCallBackData(final String callBack) {
        return callBack.substring(callBack.indexOf(" ") + 1);
    }

    private void mainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Главное меню");
        message.setReplyMarkup(getAdminKeyboardMarkup());
        send(message);
    }

    private void sendMessageMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(":zap:Вы можете использовать готовые шаблоны сообщений или создать новое сообщение!:zap:");
        message.setReplyMarkup(getSendMessageMenu());
        send(message);
    }

    private void showUsers(long chatId, int groupId, String messageText, String callbackData) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(messageText));

        var users = groupId >= 0 ? userRepository.findByGroupId(groupId) : userRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();

        for (var user : users) {
            var line = new ArrayList<InlineKeyboardButton>();
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

    private void showGroups(long chatId, String messageText, String callbackData) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(messageText));

        var groups = groupRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();

        for (var group : groups) {
            var line = new ArrayList<InlineKeyboardButton>();
            var groupButton = new InlineKeyboardButton();
            groupButton.setText(STR."\{group.getGroupName()} : \{group.getCountUsers()}чел.");
            groupButton.setCallbackData(STR."\{callbackData} \{group.getGroupId()}");
            line.add(groupButton);
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
            case FIRST_S_M -> {
                startQuietTime = LocalTime.of(6, 0, 0);
                endQuietTime = LocalTime.of(12, 0, 0);
            }
            case SECOND_S_M -> {
                startQuietTime = LocalTime.of(10, 0, 0);
                endQuietTime = LocalTime.of(20, 0, 0);
            }
            case THIRD_S_M -> {
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

        if (userRepository.existsById(chatId)) {
            message.setReplyMarkup(getUserKeyboardMarkup());
        } else if (adminRepository.existsById(chatId)) {
            message.setReplyMarkup(getAdminKeyboardMarkup());
        }

        send(message);
    }

    private void setSilenceModeCommandReceived(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode("Настройте режим \"Тишины\" - промежуток времени, когда Вы не будете получать сообщения от бота.\n\n:heavy_exclamation_mark:Время указано в соответствии с Московским временем (MSK).\n\nВыберите наиболее удобный режим: "));

        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();
        var firstLine = new ArrayList<InlineKeyboardButton>();
        var secondLine = new ArrayList<InlineKeyboardButton>();
        var thirdLine = new ArrayList<InlineKeyboardButton>();
        var fourthLine = new ArrayList<InlineKeyboardButton>();

        var firstSilenceModeButton = new InlineKeyboardButton();
        firstSilenceModeButton.setCallbackData(FIRST_S_M);
        firstSilenceModeButton.setText("с 6:00 по 12:00");
        firstLine.add(firstSilenceModeButton);

        var secondSilenceModeButton = new InlineKeyboardButton();
        secondSilenceModeButton.setCallbackData(SECOND_S_M);
        secondSilenceModeButton.setText("с 10:00 по 20:00");
        secondLine.add(secondSilenceModeButton);

        var thirdSilenceModeButton = new InlineKeyboardButton();
        thirdSilenceModeButton.setCallbackData(THIRD_S_M);
        thirdSilenceModeButton.setText("с 22:00 по 8:00");
        thirdLine.add(thirdSilenceModeButton);

        var fourthSilenceModeButton = new InlineKeyboardButton();
        fourthSilenceModeButton.setCallbackData(FOURTH_S_M);
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
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var keyboardRows = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();

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
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var keyboardRows = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();

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
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var keyboardRows = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();

        row.add(CREATE_GROUP);
        row.add(EDIT_GROUP_NAME);
        row.add(DELETE_GROUP);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(ADD_USER_TO_GROUP);
        row.add(READ_USERS_FROM_GROUP);
        row.add(DELETE_USER_FROM_GROUP);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(BACK_TO_MAIN_MENU);
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getSendMessageMenu() {
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var keyboardRows = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();

        row.add(CREATE_MESSAGE);
        row.add(USE_TEMPLATE_MESSAGE);
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
        message.setText(EmojiParser.parseToUnicode(textToSend));
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