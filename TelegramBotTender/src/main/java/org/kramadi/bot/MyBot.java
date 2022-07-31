package org.kramadi.bot;

import org.kramadi.bot.MySQL.*;
import org.kramadi.bot.document.XLS;
import org.kramadi.bot.look4tender.Search;
import org.apache.commons.io.IOUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;


import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

//import static bot.document.Docx.tendersToDocx;
import static java.lang.Math.toIntExact;

@Component

public class MyBot extends TelegramLongPollingBot {

  SearchEntity searchEntity = new SearchEntity();
  UserEntity user = new UserEntity();
  PlatformEntity platformEntity = new PlatformEntity();
  State state = State.NULL;
  final int admin_id = 554872425;

  @Value("${telegram.bot.username}")
  private String username;
  @Value("${telegram.bot.token}")
  private String token;

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.getMessage() != null && update.getMessage().hasText()) {
      Message msg = update.getMessage();
      if (HibernateUtil.selectUser("from UserEntity where id = " + msg.getFrom().getId()).size() == 0) {
        user.setId(Math.toIntExact(msg.getFrom().getId()));
        user.setFirstName(msg.getFrom().getFirstName());
        user.setLastName(msg.getFrom().getLastName());
        user.setChatId(msg.getChatId().intValue());
        user.setUsername(msg.getFrom().getUserName());
        HibernateUtil.insert(user);
      } else user = HibernateUtil.selectUser("from UserEntity where id = " + msg.getFrom().getId()).get(0);

      if (msg.getText().equalsIgnoreCase("/start")) {
        state = State.NULL;
        String start = "Привіт, " + msg.getFrom().getFirstName() + "! " + Answer.INTRO;
         //якщо користувач адмін
        if (user.getId() == admin_id)
          sendMsg(start, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES, Answer.NEW_PLATFORM), msg.getChatId());
        else
          sendMsg(start, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES), msg.getChatId());
      }

        //запит на створення нового пошуку від користувача
      else if (msg.getText().equalsIgnoreCase("/newSearch") || msg.getText().equalsIgnoreCase(Answer.NEW_SEARCH)) {
        sendMsg(Answer.CREATE_NEW_SEARCH, msg.getChatId());
        sendMsg(Answer.INPUT_SEARCH_NAME, msg.getChatId());
        state = State.inputSearchName;
      }

      //бот очікує отримати назву пошуку
      else if (state == State.inputSearchName) {
        searchEntity.setName(msg.getText());
        searchEntity.setCreationDate(Date.valueOf(LocalDate.now()));
        searchEntity.setLastSearchDate(Date.valueOf(LocalDate.now()));
          // Відправка повідомлення
        sendMsg(Answer.INPUT_KEYWORD + " \"" + msg.getText() + "\"", msg.getChatId());
        state = State.inputKeyWord;
      }

      //бот очікує отримати ключове слово
      else if (state == State.inputKeyWord) { searchEntity.setKeyword(msg.getText());
        ArrayList<PlatformEntity> platforms = HibernateUtil.selectPlatforms();
        List<String> platfomsBtn = new ArrayList<>();
        for (PlatformEntity platform : platforms) {
          platfomsBtn.add(platform.getName());
        }
            //клавіатура зі всіма завантаженими тендерними майданчиками
        sendMsg(Answer.CHOOSE_PLATFORM, platfomsBtn, msg.getChatId());
        state = State.choosePlatform;
      }

      //бот очікує отримати тендерний майданчик
      else if (state == State.choosePlatform) {
        PlatformEntity platform = HibernateUtil.selectPlatform("from PlatformEntity where name = '" + msg.getText() + "'");
        searchEntity.setPlatformByPlatformId(platform);
        sendMsg(Answer.INPUT_INTERVAL, msg.getChatId());
        state = State.inputInterval;
      }

      //бот очікує отримати інтервал оновлення та перевіряє його відповідність цілому числу
      else if (state == State.inputInterval && !msg.getText().matches(".*[^0-9].*")) {
        searchEntity.setDayInterval(Integer.parseInt(msg.getText()));
        //sendMsg("Sucess", msg.getChatId());
        searchEntity.setUserByUserId(HibernateUtil.selectUser("from UserEntity where id = " + msg.getFrom().getId()).get(0));
        int id = HibernateUtil.insert(searchEntity);
        searchEntity.setId(id);
          //здійснення пошуку
        sendMsg(Answer.SEARCHING, msg.getChatId());
        Search.execute(searchEntity);
        ArrayList<TenderEntity> tenders = HibernateUtil.selectTendersBySearch(searchEntity.getId());
        if (tenders.size()>0) {
          File doc = XLS.create(tenders);
          sendDoc(msg.getChatId(), doc, "Усі тендери з пошуку\""+searchEntity.getName()+"\" вивантажені у файл!");
        }else{
          sendMsg(Answer.TENDERS_NOT_FOUND, msg.getChatId());
        }
        //створення завдання та встановлення таймера на заданий інтервал
        TimerTask task = new SearchTask(searchEntity, user);
        Timer timer = new Timer();
        int perod = searchEntity.getDayInterval()*86400000 ;
        timer.schedule( task,perod, perod);
        //возвращение состаяния в первоначальное
        state = State.NULL;
      }

      //бот очікує отримати інтервал оновлення і він не відповідає цілій кількості
      else if ((state == State.inputInterval && msg.getText().matches(".*[^0-9].*") || (state == State.inputNewInterval && msg.getText().matches(".*[^0-9].*")))) {
        sendMsg(Answer.INPUT_INT, msg.getChatId());
      }

      //усі пошуки користувача
      else if (msg.getText().equalsIgnoreCase("/allSearches") || msg.getText().equalsIgnoreCase(Answer.ALL_SEARCHES)) {
        state =State.NULL;
        ArrayList<SearchEntity> searches = HibernateUtil.selectSearchesByUser(user.getId());
        List<String> searchesBtn = new ArrayList<>();
        for (SearchEntity search : searches){ searchesBtn.add(search.getName());
        }
        if (searchesBtn.size() == 0) sendMsg(Answer.NO_SEARCH, Arrays.asList(Answer.NEW_SEARCH), msg.getChatId());
        else {
        //надсилання повідомлення з Inline Keyboard, де кнопки – назви створених пошуків користувача
          sendMsgWithInlineKeyboard(Answer.CHOOSE_SEARCH, searchesBtn, msg.getChatId());
        }
        state = State.chooseSearch;
      }

      //редактор пошуку, бот очікує отримати нове ім'я пошуку
      else if (state == State.inputNewSearchName){ searchEntity.setName(msg.getText());
        HibernateUtil.update(searchEntity);
        sendMsg("Пошук перейменований!", msg.getChatId());
          //якщо користувач адмін
        if (user.getId() == admin_id)
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES, Answer.NEW_PLATFORM), msg.getChatId());
        else
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES), msg.getChatId());
        state = State.NULL;
      }

      //редактор пошуку, бот очікує отримати новий інтервал і перевіряє його відповідність цілій кількості
      else if (state == State.inputNewInterval && !msg.getText().matches(".*[^0-9].*")){
        searchEntity.setDayInterval(Integer.parseInt(msg.getText()));
        sendMsg("інтервал оновлено!", msg.getChatId());
        //якщо користувач адмін
        if (user.getId() == admin_id)
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES, Answer.NEW_PLATFORM), msg.getChatId());
        else
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES), msg.getChatId());
        state = State.NULL;
      }

        //додавання нового тендерного майданчика (якщо користувач адмін)
      else if (msg.getText().equalsIgnoreCase(Answer.NEW_PLATFORM)){
        sendMsg(Answer.INPUT_PLATFORM_NAME, msg.getChatId());
        state = State.inputPlatformName;
      }

      //бот очікує отримати назву для тендерного майданчика (якщо користувач адмін)
      else if (state == State.inputPlatformName){
        platformEntity.setName(msg.getText());
        sendMsg(Answer.INPUT_PLATFORM_SCRIPT, msg.getChatId());
        state = State.inputPlatformScript;
      }

      else if (msg.hasText()) {
        String text = msg.getText();
        SendMessage sm = new SendMessage();
        sm.setText("немає такої команди " + text);
        sm.setChatId(String.valueOf(msg.getChatId()));
        try {
          execute(sm);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      }

    }else if (update.hasCallbackQuery()) {
      CallbackQuery cbq = update.getCallbackQuery();
          //якщо бот очікує на вибір пошуку шляхом натискання на кнопку на inline keyboard
      if (state == State.chooseSearch && !cbq.getData().equals(Answer.BACK)) {
        searchEntity = HibernateUtil.selectSearch("from SearchEntity where name = '"	+ cbq.getData() + "' " +
                "and userByUserId.id = " + user.getId());
        editMsgWithInlineKeyboard(cbq.getData(), Arrays.asList(Answer.VIEW_SEARCH, Answer.EDIT_SEARCH, Answer.TENDERS,
                        Answer.REMOVE_SEARCH, Answer.BACK), cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
        state=State.searchOperations;
      }
      //якщо була натиснута кнопка перегляду пошуку
      else if (cbq.getData().equalsIgnoreCase(Answer.VIEW_SEARCH)){
        state = State.searchDetails;
        String s = searchEntity.getState()==1? "Включено" : "Вимкнено";
        editMsgWithInlineKeyboard(Answer.NAME + ": " +
                        searchEntity.getName() + "\n" +
                        Answer.CREATION_DATE + ": " +
                        searchEntity.getCreationDate() + "\n" +
                        Answer.LAST_SEARCH_DATE + ": " +
                        searchEntity.getLastSearchDate() + "\n" +
                        Answer.KEYWORD + ": " +searchEntity.getKeyword()
                        + "\n" +
                        Answer.INTERVAL + " (дні): " +
                        searchEntity.getDayInterval() + "\n" +
                        Answer.STATE + ": " + s + "\n",
                Arrays.asList(Answer.BACK), cbq.getMessage().getChatId(),cbq.getMessage().getMessageId());
      }
      //якщо була натиснута кнопка редагувати пошук
      else if (cbq.getData().equalsIgnoreCase(Answer.EDIT_SEARCH)){
        state = State.searchEdit; String s = Answer.SWITCH_OFF;
        if (searchEntity.getState()==0) s = Answer.SWITCH_ON;
        editMsgWithInlineKeyboard(cbq.getMessage().getText(),
                Arrays.asList(Answer.NAME, Answer.INTERVAL, s, Answer.BACK), cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
      }
      //якщо була натиснута кнопка редагувати -> найменування
      else if (cbq.getData().equalsIgnoreCase(Answer.NAME)){ editMsg(Answer.INPUT_NEW_SEARCH_NAME + "\""+cbq.getMessage().getText()+" \"",
              cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
        state = State.inputNewSearchName;
      }
      //якщо була натиснута кнопка редагувати -> інтервал
      else if (cbq.getData().equalsIgnoreCase(Answer.INTERVAL)){ editMsg(Answer.INPUT_NEW_INTERVAL + "\""+cbq.getMessage().getText()+" \"",
              cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
        state = State.inputNewInterval;
      }

      //якщо була натиснута кнопка редагувати -> увімкнути або вимкнути пошук
      else if (cbq.getData().equalsIgnoreCase(Answer.SWITCH_ON) || cbq.getData().equalsIgnoreCase(Answer.SWITCH_OFF) ){
        byte st = cbq.getData().equalsIgnoreCase(Answer.SWITCH_ON)? (byte) 1 : (byte) 0;
        searchEntity.setState(st);

        HibernateUtil.update(searchEntity);
        if (cbq.getData().equalsIgnoreCase(Answer.SWITCH_ON)) editMsg("Пошук увімкнено!", cbq.getMessage().getChatId(),
                cbq.getMessage().getMessageId());
          else editMsg("Пошук вимкнено!", cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
  //якщо користувач адмін
        if (user.getId() == admin_id)
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES, Answer.NEW_PLATFORM),
                  cbq.getMessage().getChatId());
        else
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES), cbq.getMessage().getChatId());
        state = State.NULL;
      }
      //якщо була натиснута кнопка видалити пошук
      else if (cbq.getData().equalsIgnoreCase(Answer.REMOVE_SEARCH)){
        HibernateUtil.deleteAll("delete TenderEntity where search_id = " + searchEntity.getId());
        HibernateUtil.delete("from SearchEntity where id = " + searchEntity.getId());
        editMsg("Пошук \"" + cbq.getMessage().getText() + "\" успішно видалено!",
                cbq.getMessage().getChatId(),
                cbq.getMessage().getMessageId() );
  //якщо користувач адмін
        if (user.getId() == admin_id)
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES, Answer.NEW_PLATFORM),
                  cbq.getMessage().getChatId());
        else
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES), cbq.getMessage().getChatId());
      }

      //якщо була натиснута кнопка тендери
      else if (cbq.getData().equalsIgnoreCase(Answer.TENDERS)){
        ArrayList<TenderEntity> tenders = HibernateUtil.selectTendersBySearch(searchEntity.getId());
        if (tenders.size()>0) {
          File doc = XLS.create(tenders);
          editMsg("Усі тендери з пошуку\""+searchEntity.getName()+"\" вивантажені у файл!",
                  cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
          sendDoc(cbq.getMessage().getChatId(), doc, "");
        }else {
          editMsgWithInlineKeyboard(Answer.EMPTY_TENDER_LIST, Arrays.asList(Answer.BACK),
                  cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
        }
      }
        // якщо була натиснута кнопка назад
      else if (cbq.getData().equalsIgnoreCase(Answer.BACK)){ System.out.println(state);
        switch (state){
          case NULL:
            break;
          case searchOperations: ArrayList<SearchEntity> searches =
                  HibernateUtil.selectSearchesByUser(user.getId());
            List<String> searchesBtn = new ArrayList<>();
            for (SearchEntity search : searches){ searchesBtn.add(search.getName());
            }
            if (searchesBtn.size() == 0)

              editMsgWithInlineKeyboard(Answer.NO_SEARCH, Arrays.asList(Answer.NEW_SEARCH),
                      cbq.getMessage().getChatId(),
                      cbq.getMessage().getMessageId());
            else editMsgWithInlineKeyboard(Answer.CHOOSE_SEARCH, searchesBtn, cbq.getMessage().getChatId(),
                    cbq.getMessage().getMessageId());
            state = State.chooseSearch;
            break;
          case searchEmptyTenders:
          case searchEdit:
          case searchDetails: editMsgWithInlineKeyboard(searchEntity.getName(),
                  Arrays.asList(Answer.VIEW_SEARCH, Answer.EDIT_SEARCH, Answer.TENDERS,
                          Answer.REMOVE_SEARCH, Answer.BACK), cbq.getMessage().getChatId(), cbq.getMessage().getMessageId());
            state=State.searchOperations; break;
        }
      }
    }
    else if (update.hasMessage() && update.getMessage().hasDocument()){
      //бот очікує отримати документ зі скриптом для нового тендерного майданчика (якщо користувач адмін)
      if (state == State.inputPlatformScript ){
        File f=new File("file.txt");
        GetFile getFileReq = new GetFile();
        getFileReq.setFileId(update.getMessage().getDocument().getFileId());
        try {
          org.telegram.telegrambots.meta.api.objects.File file = execute(getFileReq);
          String url = file.getFileUrl(getBotToken(),file.getFilePath());
          System.out.println(url);
          downloadFileFromURL(url, f);
          try(FileInputStream inputStream = new FileInputStream(f)){
            String script = IOUtils.toString(inputStream);
            platformEntity.setScript(script);
            HibernateUtil.insert(platformEntity);
          }
        }catch (TelegramApiException ex){
          ex.printStackTrace();
        } catch (IOException ex) {
          ex.printStackTrace();
        }sendMsg(Answer.SCRIPT_DOWNLOADED, update.getMessage().getChatId());
        if (user.getId() == admin_id)
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES, Answer.NEW_PLATFORM),
                  update.getMessage().getChatId());
        else
          sendMsg(Answer.CONTINUE, Arrays.asList(Answer.NEW_SEARCH, Answer.ALL_SEARCHES), update.getMessage().getChatId());
        state = State.NULL;
      }
    }
  }

  //надсилання документа в чат
  private void sendDoc(Long chatId, File save, String text) {
    SendDocument sendDocReq = new SendDocument();
    sendDocReq.setChatId(chatId.toString());
    sendDocReq.setCaption(text);
    InputFile newFile = new InputFile(save);
    sendDocReq.setDocument(newFile);
    try {
      execute(sendDocReq);
    }
    catch (TelegramApiException ex) {ex.printStackTrace();
    }
  }

  //редагувати повідомлення
  private void editMsg(String newText, Long chatId, Integer msgId) {
    EditMessageText newMsg = new EditMessageText();
    newMsg.setChatId(chatId.toString());
    newMsg.setMessageId(toIntExact(msgId));
    newMsg.setText(newText);
    try {
      execute(newMsg);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  //надіслати текстове повідомлення до чату та встановити клавіатуру з варіантами відповіді (ReplyKeyboard)
  public void sendMsg (String text, List<String> buttons, Long chatId) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.enableMarkdown(true);
// Створюємо клавіатуру
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    replyKeyboardMarkup.setSelective(true);
    replyKeyboardMarkup.setResizeKeyboard(true);
    replyKeyboardMarkup.setOneTimeKeyboard(true);
// Створюємо список рядків клавіатури
    List<KeyboardRow> keyboard = new ArrayList<>();
// Перший рядок клавіатури
    Iterator<String> iButtons = buttons.iterator();
    while (iButtons.hasNext()){
      KeyboardRow keyboardRow = new KeyboardRow();
// Додаємо кнопки в перший рядок клавіатури
      keyboardRow.add(iButtons.next());
      if(iButtons.hasNext()) keyboardRow.add(iButtons.next());
      keyboard.add(keyboardRow);
    }
// та встановлюємо цей список нашої клавіатури
    replyKeyboardMarkup.setKeyboard(keyboard);
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText(text);
    try { execute(sendMessage);
    } catch (TelegramApiException e) { e.printStackTrace();
    }
  }

  //надіслати текстове повідомлення в чат і встановити InlineKeyboard
  public void sendMsgWithInlineKeyboard (String text, List<String> buttons, Long chatId) {
    SendMessage sendMessage = new SendMessage();
// Створюємо клавіатуру
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
// Створюємо список рядків клавіатури
    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
// Перший рядок клавіатури
    Iterator<String> iButtons = buttons.iterator();
    while (iButtons.hasNext()) {
      List<InlineKeyboardButton> rowInline = new ArrayList<>();
// Додаємо кнопки в перший рядок клавіатури
      String textBtn = iButtons.next();
      rowInline.add(InlineKeyboardButton.builder().text(textBtn).callbackData(textBtn).build());
      if (iButtons.hasNext()) {
        textBtn = iButtons.next();
        rowInline.add(InlineKeyboardButton.builder().text(textBtn).callbackData(textBtn).build());
      }
      rowsInline.add(rowInline);
    }
// та встановлюємо цей список нашої клавіатури
    sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    inlineKeyboardMarkup.setKeyboard(rowsInline);
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText(text);
    try { execute(sendMessage);
    } catch (TelegramApiException e) { e.printStackTrace();
    }
  }

  //редагувати повідомлення та встановити InlineKeyboard
  private void editMsgWithInlineKeyboard(String newText,	List<String> buttons, Long chatId, Integer msgId) {
    EditMessageText newMsg = EditMessageText.builder().chatId(chatId.toString()).text(newText).messageId(toIntExact(msgId)).build();
// Створюємо клавіатуру
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
// Створюємо список рядків клавіатури
    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
// Перший рядок клавіатури
    Iterator<String> iButtons = buttons.iterator();
    while (iButtons.hasNext()) {
      List<InlineKeyboardButton> rowInline = new ArrayList<>();
// Додаємо кнопки в перший рядок клавіатури
      String textBtn = iButtons.next();
      rowInline.add(InlineKeyboardButton.builder().text(textBtn).callbackData(textBtn).build());
      if (iButtons.hasNext()) { textBtn = iButtons.next();
        rowInline.add(InlineKeyboardButton.builder().text(textBtn).callbackData(textBtn).build());
      }
      rowsInline.add(rowInline);
    }
// та встановлюємо цей список нашої клавіатури
    newMsg.setReplyMarkup(inlineKeyboardMarkup);
    inlineKeyboardMarkup.setKeyboard(rowsInline);
    try {
      execute(newMsg);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public static void downloadFileFromURL(String url, File destination) {
    try {
      URL website = new URL(url);
      ReadableByteChannel rbc;
      rbc = Channels.newChannel(website.openStream());
      FileOutputStream fos = new FileOutputStream(destination);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      fos.close();
      rbc.close();
    } catch (IOException e) { e.printStackTrace();
    }
  }

  private void sendMsg( String text, Long chatId) { SendMessage sendMessage = new SendMessage();
    sendMessage.enableMarkdown(true);
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText(text);
    try {
      execute(sendMessage);
    } catch (TelegramApiException e) { e.printStackTrace();
    }
  }

  private class SearchTask extends TimerTask {
    SearchEntity searchEntity;
    UserEntity user;
    public SearchTask (SearchEntity searchEntity, UserEntity user){
      this.searchEntity=searchEntity;
      this.user = user;
    }

    @Override
    public void run() {
      System.out.println("Запуск завдання111");
      boolean changed = Search.execute(searchEntity);
      ArrayList<TenderEntity> tendersAfter = HibernateUtil.selectTendersBySearch(searchEntity.getId());
            //если найдены новые тендеры, отправляем сообщение
      if (changed) {
        System.out.println("знайдені");
        sendMsg(Answer.NEW_TENDERS_FOUND, user.getChatId().longValue());
        File doc = XLS.create(tendersAfter);
        sendDoc(user.getChatId().longValue(), doc, "За пошуком " + "\"" + searchEntity.getName() + "\"" + Answer.NEW_TENDERS_FOUND);
      }
    }
  }
}
