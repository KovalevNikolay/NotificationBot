package spbsut.kovalev.NotificationBot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
import spbsut.kovalev.NotificationBot.entity.Scheduler;
import spbsut.kovalev.NotificationBot.entity.SilenceMode;
import spbsut.kovalev.NotificationBot.entity.User;
import spbsut.kovalev.NotificationBot.repository.AdminRepository;
import spbsut.kovalev.NotificationBot.repository.GroupRepository;
import spbsut.kovalev.NotificationBot.repository.MessageRepository;
import spbsut.kovalev.NotificationBot.repository.SchedulerRepository;
import spbsut.kovalev.NotificationBot.repository.SilenceModeRepository;
import spbsut.kovalev.NotificationBot.repository.TemplateRepository;
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
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SchedulerRepository schedulerRepository;
    @Autowired
    private SilenceModeRepository silenceModeRepository;
    private static final String BOT_NAME = "NotificationBot";
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private static final String START_CMD = "/start";
    private static final String CREATE_GROUP_CMD = "/createGroup";
    private static final String CHANGE_GROUP_NAME_CMD = "/changeGroupName";
    private static final String CREATE_TEMPLATE_CMD = "/createTemplate";
    private static final String SEND_CMD = "/send";
    private static final String COMMAND_NOT_RECOGNIZED = ":warning:Извините, команда не была распознана!:warning:";

    private static final String READ_MY_DATA = EmojiParser.parseToUnicode(":floppy_disk:Мои данные");
    private static final String DELETE_MY_DATA = EmojiParser.parseToUnicode(":x:Удалить мои данные");
    private static final String SET_SILENCE_MODE = EmojiParser.parseToUnicode(":bell:Настроить режим \"Не беспокоить\"");
    private static final String USER_GROUPS = EmojiParser.parseToUnicode(":family_man_woman_girl_boy:Группы пользователей");
    private static final String ALL_USERS = EmojiParser.parseToUnicode(":man_technologist:Все пользователи");
    private static final String APPOINT_AN_ADMIN = EmojiParser.parseToUnicode(":game_die:Назначить администратора");
    private static final String ADD_USER_TO_GROUP = EmojiParser.parseToUnicode(":man_technologist:Добавить пользователя в группу");
    private static final String DELETE_USER_FROM_GROUP = EmojiParser.parseToUnicode(":x:Удалить пользователя из группы");
    private static final String READ_USERS_FROM_GROUP = EmojiParser.parseToUnicode(":eyes:Показать пользователей");
    private static final String CREATE_GROUP = EmojiParser.parseToUnicode(":new:Создать группу");
    private static final String EDIT_GROUP_NAME = EmojiParser.parseToUnicode(":gear:Изменить группу");
    private static final String DELETE_GROUP = EmojiParser.parseToUnicode(":x:Удалить группу");
    private static final String CREATE_TEMPLATE = EmojiParser.parseToUnicode(":envelope:Шаблоны сообщений");
    private static final String MESSAGE_HISTORY = EmojiParser.parseToUnicode(":scroll:История сообщений");
    private static final String SEND_MESSAGE = EmojiParser.parseToUnicode(":envelope_with_arrow:Отправить сообщение");
    private static final String CREATE_MESSAGE = EmojiParser.parseToUnicode(":new:Новое сообщение");
    private static final String USE_TEMPLATE_MESSAGE = EmojiParser.parseToUnicode(":envelope:Использовать шаблон");
    private static final String BACK_TO_MAIN_MENU = EmojiParser.parseToUnicode(":back:В главное меню");

    private static final String SILENCE_MODE = "SILENCE_MODE";
    private static final String READ_USER_BUTTON = "READ_USER_BUTTON";
    private static final String DELETE_USER_BUTTON = "DELETE_USER_BUTTON";
    private static final String ADD_USER_BUTTON = "ADD_USER_BUTTON";
    private static final String MAKE_AN_ADMIN = "MAKE_AN_ADMIN";
    private static final String TEMPLATE_BUTTON = "TEMPLATE_BUTTON";
    private static final String FOR_SENDING = "FOR_SENDING";
    private static final String GROUP_CHANGE_NAME = "GROUP_CHANGE_NAME";
    private static final String GROUP_ADD_USER = "GROUP_ADD_USER";
    private static final String GROUP_DELETE_USER = "GROUP_DELETE_USER";
    private static final String GROUP_READ_USER = "GROUP_READ_USER";
    private static final String GROUP_DELETED = "GROUP_DELETED";

    private static final String LIST_GROUP_USERS = ":sparkle:Список пользователей группы:sparkle:";
    private static final String LIST_ALL_USERS = ":man_technologist:Список всех пользователей:";
    private static final String SEND_MESSAGE_INFO = STR."Для того, чтобы отправить сообщение введите команду \{SEND_CMD} после которой текст, который Вы хотите отправить.\n\nНапример:\n\{SEND_CMD} Hello World!";
    private static final String CREATE_GROUP_INFO = STR."Для того, чтобы создать новую группу введите команду \{CREATE_GROUP_CMD} и название группы.\n\nНапример:\n\{CREATE_GROUP_CMD} Моя Группа .";
    private static final String CHANGE_GROUP_NAME_INFO = STR."Для того, чтобы изменить название группы введите команду \{CHANGE_GROUP_NAME_CMD} и новое название для группы. \n\nНапример:\n\{CHANGE_GROUP_NAME_CMD} Новое название.";
    private static final String CREATE_TEMPLATE_INFO = STR."Для того, чтобы создать шаблон сообщения введите команду \{CREATE_TEMPLATE_CMD} и текст сообщения.\n\nНапример:\n\{CREATE_TEMPLATE_CMD} текст сообщения.";
    private static final String DATA_IS_DELETED = ":x:Данные удалены!:x:";
    private static final String THIS_NAME_ALREADY_EXISTS = ":x:Группа с таким названием уже существует!:x:";
    private static final String TEMPLATE_CREATED = ":heavy_check_mark: Шаблон сообщения создан!";
    private static final String TEMPLATE_WAS_NOT_CREATED = ":x:Шаблон с таким сообщением уже существует!:x:";
    private static final String SELECT_GROUP_FOR_READ = ":point_right:Выберите группу, пользователей которой необходимо показать:point_left:";
    private static final String SELECT_GROUP_FOR_DELETE = ":point_right:Выберите группу, из которой хотите удалить пользователя:point_left:";
    private static final String SELECT_GROUP_FOR_ADD = ":point_right:Выберите группу, в которую хотите добавить пользователя:point_left:";
    private static final String SELECT_GROUP_ON_DELETE = ":point_right:Выберите группу, которую хотите удалить:point_left:";
    private static final String SELECT_GROUP_FOR_SENDING = ":point_right:Выберите группу, которой хотите отправить сообщение:point_left:";
    private static final String SELECT_GROUP_FOR_CHANGE = ":point_right:Выберите группу, название которой хотите изменить:point_left:";
    private static final String SELECT_TEMPLATE = ":point_right:Выберите шаблон сообщения, который хотите использовать:point_left:";
    private static final String SELECT_USER_FOR_ADD = ":point_right:Выберите пользователя, которого хотите добавить в группу:point_left:";
    private static final String CHANGED_SUCCESSFULLY = "Название группы изменено на ";

    private Integer idGroup;
    private String messageForSending;
    private String groupName;

    public TelegramBot() {
        super(BOT_TOKEN);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            if (adminRepository.existsById(chatId)) {
                processingAdminCommands(update);
            } else {
                processingUserCommands(update);
            }
        } else if (update.hasCallbackQuery()) {
            processingCallBackQuery(update);
        }
    }

    private void processingCallBackQuery(Update update) {
        String callBackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (callBackData.contains(SILENCE_MODE)) {
            int silenceModeId = Integer.parseInt(getIdFromCallBackData(callBackData));
            setSilenceModeForUser(chatId, silenceModeId);
            sendTheSelectedMode(chatId, silenceModeId);
        } else if (callBackData.contains(READ_USER_BUTTON)) {
            long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
            readUserData(chatId, selectedChatId);
        } else if (callBackData.contains(MAKE_AN_ADMIN)) {
            long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
            appointAnAdministrator(selectedChatId);
            sendMessage(chatId, STR.":heavy_check_mark:Пользователь \{selectedChatId} назначен администратором!");
        } else if (callBackData.contains(GROUP_READ_USER)) {
            int selectedGroupId = Integer.parseInt(getIdFromCallBackData(callBackData));
            showUsers(chatId, selectedGroupId, LIST_GROUP_USERS, READ_USER_BUTTON);
        } else if (callBackData.contains(GROUP_DELETE_USER)) {
            idGroup = Integer.parseInt(getIdFromCallBackData(callBackData));
            showUsers(chatId, idGroup, LIST_GROUP_USERS, DELETE_USER_BUTTON);
        } else if (callBackData.contains(GROUP_ADD_USER)) {
            idGroup = Integer.parseInt(getIdFromCallBackData(callBackData));
            showUsers(chatId, 0, SELECT_USER_FOR_ADD, ADD_USER_BUTTON);
        } else if (callBackData.contains(DELETE_USER_BUTTON)) {
            long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
            deleteUserFromGroup(selectedChatId);
            changeCountUsersInGroup();
            sendMessage(chatId, STR."Пользователь \{selectedChatId} удален из группы");
        } else if (callBackData.contains(ADD_USER_BUTTON)) {
            long selectedChatId = Long.parseLong(getIdFromCallBackData(callBackData));
            addUserToGroup(selectedChatId);
            changeCountUsersInGroup();
            sendMessage(chatId, STR."Пользователь \{selectedChatId} добавлен в группу");
        } else if (callBackData.contains(GROUP_DELETED)) {
            int selectedGroupId = Integer.parseInt(getIdFromCallBackData(callBackData));
            deleteGroup(selectedGroupId);
            sendMessage(chatId, STR.":x:Группа \{groupName} удалена!:x:");
        } else if (callBackData.contains(GROUP_CHANGE_NAME)) {
            idGroup = Integer.parseInt(getIdFromCallBackData(callBackData));
            sendMessage(chatId, CHANGE_GROUP_NAME_INFO);
        } else if (callBackData.contains(TEMPLATE_BUTTON)) {
            int templateId = Integer.parseInt(getIdFromCallBackData(callBackData));
            getMessageTextFromTemplate(templateId);
            showGroups(chatId, SELECT_GROUP_FOR_SENDING, FOR_SENDING);
        } else if (callBackData.contains(FOR_SENDING)) {
            int selectedGroupId = Integer.parseInt(getIdFromCallBackData(callBackData));
            sendMessageToUsersOfGroup(selectedGroupId);
            writeToTheMessageHistory(chatId);
            sendMessage(chatId, ":white_check_mark:Сообщение отправлено!");
        }
    }

    private void processingUserCommands(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.equals(START_CMD)) {
            startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            registerUser(update.getMessage());
        } else if (messageText.equals(SET_SILENCE_MODE)) {
            setSilenceModeCommandReceived(chatId);
        } else if (messageText.equals(READ_MY_DATA)) {
            readUserData(chatId, chatId);
        } else if (messageText.equals(DELETE_MY_DATA)) {
            deleteUserData(chatId);
            sendMessage(chatId, DATA_IS_DELETED);
        } else {
            sendMessage(chatId, COMMAND_NOT_RECOGNIZED);
        }
    }

    private void processingAdminCommands(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

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
            showGroups(chatId, "Группы пользователей: ", "#");
            sendMessage(chatId, CREATE_GROUP_INFO);
        } else if (messageText.equals(CREATE_TEMPLATE)) {
            showTemplates(chatId, "#", "Шаблоны сообщений:");
            sendMessage(chatId, CREATE_TEMPLATE_INFO);
        } else if (messageText.contains(CREATE_GROUP_CMD)) {
            String groupName = parseTextFromMessage(messageText);
            String resultMessage = createGroup(groupName)
                    ? STR.":heavy_check_mark: Группа \{groupName} создана!"
                    : THIS_NAME_ALREADY_EXISTS;
            sendMessage(chatId, resultMessage);
        } else if (messageText.contains(CREATE_TEMPLATE_CMD)) {
            String templateMessage = parseTextFromMessage(messageText);
            String resultMessage = createTemplateMessage(templateMessage)
                    ? TEMPLATE_CREATED
                    : TEMPLATE_WAS_NOT_CREATED;
            sendMessage(chatId, resultMessage);
        } else if (messageText.contains(CHANGE_GROUP_NAME_CMD)) {
            String newGroupName = parseTextFromMessage(messageText);
            String result = changeGroupName(newGroupName)
                    ? CHANGED_SUCCESSFULLY + newGroupName
                    : THIS_NAME_ALREADY_EXISTS;
            sendMessage(chatId, result);
        } else if (messageText.contains(SEND_CMD)) {
            messageForSending = parseTextFromMessage(messageText);
            showGroups(chatId, SELECT_GROUP_FOR_SENDING, FOR_SENDING);
        } else if (messageText.equals(EDIT_GROUP_NAME)) {
            showGroups(chatId, SELECT_GROUP_FOR_CHANGE, GROUP_CHANGE_NAME);
        } else if (messageText.equals(READ_USERS_FROM_GROUP)) {
            showGroups(chatId, SELECT_GROUP_FOR_READ, GROUP_READ_USER);
        } else if (messageText.equals(DELETE_GROUP)) {
            showGroups(chatId, SELECT_GROUP_ON_DELETE, GROUP_DELETED);
        } else if (messageText.equals(ADD_USER_TO_GROUP)) {
            showGroups(chatId, SELECT_GROUP_FOR_ADD, GROUP_ADD_USER);
        } else if (messageText.equals(DELETE_USER_FROM_GROUP)) {
            showGroups(chatId, SELECT_GROUP_FOR_DELETE, GROUP_DELETE_USER);
        } else if (messageText.equals(SEND_MESSAGE)) {
            sendMessageMenu(chatId);
        } else if (messageText.equals(USE_TEMPLATE_MESSAGE)) {
            showTemplates(chatId, TEMPLATE_BUTTON, SELECT_TEMPLATE);
        } else if (messageText.equals(CREATE_MESSAGE)) {
            sendMessage(chatId, SEND_MESSAGE_INFO);
        } else if (messageText.equals(MESSAGE_HISTORY)) {
            showMessageHistory(chatId);
        } else {
            sendMessage(chatId, COMMAND_NOT_RECOGNIZED);
        }
    }

    @Scheduled(cron = "0 1 * * * ?")
    private void sendMessagesFromTheScheduler() {
        var scheduler = schedulerRepository.findAll();
        for (var schedule : scheduler) {
            if (!isBetween(schedule.getStartQuietTime(), schedule.getEndQuietTime())) {
                sendMessage(schedule.getUserId(), schedule.getMessageText());
                schedulerRepository.deleteById(schedule.getId());
            }
        }
    }

    private boolean isBetween(LocalTime startTime, LocalTime endTime) {
        LocalTime currentTime = LocalTime.now();
        if (startTime == null || endTime == null) {
            return false;
        } else if (startTime.isBefore(endTime)) {
            return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
        } else {
            return currentTime.isAfter(startTime) || currentTime.isBefore(endTime);
        }
    }

    private void showMessageHistory(final long chatId) {
        var history = messageRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();
        var message = new SendMessage();

        for (var msg : history) {
            var line = new ArrayList<InlineKeyboardButton>();
            var messageButton = new InlineKeyboardButton();
            String text = msg.getMessageText();
            text = text.length() > 30 ? STR."\{text.substring(0, 30)}..." : text;
            messageButton.setText(text);
            messageButton.setCallbackData("#");
            line.add(messageButton);
            rowsInLine.add(line);
        }

        markup.setKeyboard(rowsInLine);
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(":warning:История отправленных сообщений:"));
        message.setReplyMarkup(markup);
        send(message);
    }

    private void sendTheSelectedMode(final long chatId, final int silenceModeId) {
        if (silenceModeRepository.findById(silenceModeId).isPresent()) {
            var mode = silenceModeRepository.findById(silenceModeId).get();
            var message = new SendMessage();
            var text = (mode.getStartQuietTime() == null || mode.getEndQuietTime() == null)
                    ? "получать сообщения всегда"
                    : STR."с \{mode.getStartQuietTime()} по \{mode.getEndQuietTime()}";
            message.setChatId(chatId);
            message.setText(STR."Вы выбрали настройку: \{text}");
            send(message);
        }
    }

    private void sendMessageToUsersOfGroup(final int selectedGroupId) {
        var users = userRepository.findByGroupId(selectedGroupId);
        var schedulers = new ArrayList<Scheduler>();
        for (var user : users) {
            if (isBetween(user.getStartQuietTime(), user.getEndQuietTime())) {
                var scheduler = new Scheduler();
                scheduler.setUserId(user.getChatId());
                scheduler.setStartQuietTime(user.getStartQuietTime());
                scheduler.setEndQuietTime(user.getEndQuietTime());
                scheduler.setMessageText(messageForSending);
                schedulers.add(scheduler);
            } else {
                sendMessage(user.getChatId(), messageForSending);
            }
        }
        schedulerRepository.saveAll(schedulers);
    }

    private void writeToTheMessageHistory(final long chatId) {
        var message = new spbsut.kovalev.NotificationBot.entity.Message();
        message.setMessageText(messageForSending);
        message.setSenderId(chatId);
        message.setTimeSending(new Timestamp(System.currentTimeMillis()));
        messageRepository.save(message);
    }

    private void showTemplates(final long chatId, final String callback, final String messageInfo) {
        var templates = templateRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();
        var message = new SendMessage();

        for (var template : templates) {
            var templateButton = new InlineKeyboardButton();
            String text = template.getMessageText();
            text = text.length() > 30 ? STR."\{text.substring(0, 30)}..." : text;
            templateButton.setText(text);
            templateButton.setCallbackData(STR."\{callback} \{template.getTemplateId()}");
            var line = List.of(templateButton);
            rowsInLine.add(line);
        }

        markup.setKeyboard(rowsInLine);
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(messageInfo));
        message.setReplyMarkup(markup);
        send(message);
    }

    private void getMessageTextFromTemplate(final int templateId) {
        if (templateRepository.findById(templateId).isPresent()) {
            var template = templateRepository.findById(templateId).get();
            messageForSending = template.getMessageText();
        }
    }

    private boolean changeGroupName(final String newGroupName) {
        if (groupRepository.findById(idGroup).isPresent() && groupRepository.findByGroupName(newGroupName).isEmpty()) {
            Group group = groupRepository.findById(idGroup).get();
            group.setGroupName(newGroupName);
            groupRepository.save(group);
            return true;
        }
        return false;
    }

    private boolean createTemplateMessage(final String templateMessage) {
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
            groupName = groupRepository.findById(selectedGroupId).get().getGroupName();
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

    private void changeCountUsersInGroup() {
        if (groupRepository.findById(idGroup).isPresent()) {
            var group = groupRepository.findById(idGroup).get();
            int countUsers = getCountUsersInGroup(idGroup);
            group.setCountUsers(countUsers);
            groupRepository.save(group);
            idGroup = 0;
        }
    }

    private int getCountUsersInGroup(final int groupId) {
        var usersIterator = userRepository.findByGroupId(groupId).iterator();
        int count = 0;
        while (usersIterator.hasNext()) {
            usersIterator.next();
            count++;
        }
        return count;
    }

    private void addUserToGroup(final long selectedChatId) {
        if (userRepository.findById(selectedChatId).isPresent()) {
            User user = userRepository.findById(selectedChatId).get();
            user.setGroupId(idGroup);
            userRepository.save(user);
        }
    }

    private boolean createGroup(final String groupName) {
        if (groupRepository.findByGroupName(groupName).isEmpty()) {
            Group group = new Group();
            group.setGroupName(groupName);
            group.setCountUsers(0);
            groupRepository.save(group);
            return true;
        }
        return false;
    }

    private String parseTextFromMessage(final String messageText) {
        return messageText.substring(messageText.indexOf(" ") + 1);
    }

    private void appointAnAdministrator(final long selectedChatId) {
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

    private void mainMenu(final long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Главное меню");
        message.setReplyMarkup(getAdminKeyboardMarkup());
        send(message);
    }

    private void sendMessageMenu(final long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(":zap:Вы можете использовать готовые шаблоны сообщений или создать новое сообщение!:zap:"));
        message.setReplyMarkup(getSendMessageMenu());
        send(message);
    }

    private void showUsers(final long chatId, final int groupId, final String text, final String callback) {
        var message = new SendMessage();
        var users = groupId >= 0 ? userRepository.findByGroupId(groupId) : userRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();

        for (var user : users) {
            var userButton = new InlineKeyboardButton();
            userButton.setText(STR."\{user.getChatId()} \{user.getUserName()}");
            userButton.setCallbackData(STR."\{callback} \{user.getChatId()}");
            var line = List.of(userButton);
            rowsInLine.add(line);
        }
        markup.setKeyboard(rowsInLine);
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(text));
        message.setReplyMarkup(markup);
        send(message);
    }

    private void showGroups(final long chatId, final String text, final String callback) {
        var message = new SendMessage();
        var groups = groupRepository.findAll();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();

        for (var group : groups) {
            var groupButton = new InlineKeyboardButton();
            groupButton.setText(STR."\{group.getGroupName()} : \{group.getCountUsers()}чел.");
            groupButton.setCallbackData(STR."\{callback} \{group.getGroupId()}");
            var line = List.of(groupButton);
            rowsInLine.add(line);
        }
        markup.setKeyboard(rowsInLine);
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(text));
        message.setReplyMarkup(markup);
        send(message);
    }

    private void userGroupCommandRecieved(final long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы перешли в меню управления группами пользователей!");
        message.setReplyMarkup(getGroupMenu());
        send(message);
    }

    private void setSilenceModeForUser(final long chatId, final int silenceModeId) {

        LocalTime startQuietTime = null;
        LocalTime endQuietTime = null;

        if (silenceModeRepository.findById(silenceModeId).isPresent()) {
            var silenceMode = silenceModeRepository.findById(silenceModeId).get();
            startQuietTime = silenceMode.getStartQuietTime();
            endQuietTime = silenceMode.getEndQuietTime();
        }

        if (userRepository.findById(chatId).isPresent()) {
            var user = userRepository.findById(chatId).get();
            user.setStartQuietTime(startQuietTime);
            user.setEndQuietTime(endQuietTime);
            userRepository.save(user);
        }
    }

    private void deleteUserData(final long chatId) {
        if (userRepository.existsById(chatId)) {
            userRepository.deleteById(chatId);
        }
    }

    private void readUserData(final long chatId, final long userDataId) {
        if (userRepository.findById(userDataId).isPresent()) {
            var user = userRepository.findById(userDataId).get();
            String userData = STR."Идентификатор: \{user.getChatId()}\nИмя: \{user.getFirstName()}\nФамилия: \{user.getLastName()}\nUsername: \{user.getUserName()}\nО себе: \{user.getBio()}\nГруппа: \{user.getGroupId()}\nНе беспокоить с \{user.getStartQuietTime()} по \{user.getEndQuietTime()}\nДата регистрации: \{user.getRegisteredAt()}";
            sendMessage(chatId, userData);
        }
    }

    private void registerUser(final Message message) {
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

    private void startCommandReceived(final long chatId, final String firstName) {
        var answer = EmojiParser.parseToUnicode(STR."Привет, \{firstName}, приятно познакомиться!:blush:");
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(answer);

        if (adminRepository.existsById(chatId)) {
            message.setReplyMarkup(getAdminKeyboardMarkup());
        } else {
            message.setReplyMarkup(getUserKeyboardMarkup());
        }

        send(message);
    }

    private void setSilenceModeCommandReceived(final long chatId) {
        var message = new SendMessage();
        var markup = new InlineKeyboardMarkup();
        var rowsInLine = new ArrayList<List<InlineKeyboardButton>>();
        var silenceModes = silenceModeRepository.findAll();

        for (var mode : silenceModes) {
            final var line = getInlineKeyboardButtons(mode);
            rowsInLine.add(line);
        }

        markup.setKeyboard(rowsInLine);
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(""" 
                Настройте режим "Не беспокоить" - промежуток времени, когда Вы не будете получать сообщения от бота.
                \n:heavy_exclamation_mark:Время указано в соответствии с Московским временем (MSK).
                \nВыберите наиболее удобный режим:"""));
        message.setReplyMarkup(markup);
        send(message);
    }

    private List<InlineKeyboardButton> getInlineKeyboardButtons(final SilenceMode mode) {
        var silenceModeButton = new InlineKeyboardButton();
        if (mode.getStartQuietTime() == null || mode.getEndQuietTime() == null) {
            silenceModeButton.setText("Получать сообщения всегда!");
        } else {
            silenceModeButton.setText(STR."с \{mode.getStartQuietTime()} по \{mode.getEndQuietTime()}");
        }
        silenceModeButton.setCallbackData(STR."\{SILENCE_MODE} \{mode.getId()}");
        return List.of(silenceModeButton);
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

        row.addAll(List.of(CREATE_TEMPLATE, SEND_MESSAGE, MESSAGE_HISTORY));
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.addAll(List.of(ALL_USERS, USER_GROUPS, APPOINT_AN_ADMIN));
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getGroupMenu() {
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var keyboardRows = new ArrayList<KeyboardRow>();
        var row = new KeyboardRow();

        row.addAll(List.of(CREATE_GROUP, EDIT_GROUP_NAME, DELETE_GROUP));
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.addAll(List.of(ADD_USER_TO_GROUP, READ_USERS_FROM_GROUP, DELETE_USER_FROM_GROUP));
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
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(textToSend));
        send(message);
        log.info(STR."Бот ответил пользователю: \{chatId}");
    }

    private void send(final SendMessage message) {
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