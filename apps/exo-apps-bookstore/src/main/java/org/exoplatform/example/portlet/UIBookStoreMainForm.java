package org.exoplatform.example.portlet;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.example.BookStoreService;
import org.exoplatform.example.entity.Book;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.example.portlet.UIAddBookForm;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/groovy/webui/BookStorePortlet/BookStoreMainForm.gtmpl", events = {
    @EventConfig(listeners = UIBookStoreMainForm.AddBookActionListener.class),
    @EventConfig(listeners = UIBookStoreMainForm.EditBookActionListener.class),
    @EventConfig(listeners = UIBookStoreMainForm.DeleteBookActionListener.class) })
public class UIBookStoreMainForm extends UIForm {
  private static final Log   log                            = ExoLogger.getLogger(UIBookStoreMainForm.class);

  private List<Book>         bookList                       = new ArrayList<Book>();

  public static final String COMPONENT_ID_UI_POPUP_BOOK = "UIPopupBook";

  private BookStoreService   bookService;

  public UIBookStoreMainForm() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    bookService = (BookStoreService) container.getComponentInstanceOfType(BookStoreService.class);
    if (bookService == null) {
      log.warn("cannot find bookService");
    }

    UIPopupWindow uiPopupWindow = createUIComponent(UIPopupWindow.class,
                                                    null,
                                                    COMPONENT_ID_UI_POPUP_BOOK);
    uiPopupWindow.setShow(false);
    uiPopupWindow.setWindowSize(400, 0);

    addChild(uiPopupWindow);

  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    bookList = bookService.getAll();
    super.processRender(context);
  }

  public List<Book> getBookList() {
    return bookList;
  }

  public void setBookList(List<Book> bookList) {
    this.bookList = bookList;
  }

  public BookStoreService getBookService() {
    return bookService;
  }

  public static class AddBookActionListener extends EventListener<UIBookStoreMainForm> {
    public void execute(Event<UIBookStoreMainForm> event) throws Exception {

      UIBookStoreMainForm uiMainForm = event.getSource();
      UIPopupWindow uiPopupWindow = uiMainForm.getChild(UIPopupWindow.class);
      
      UIAddBookForm uiBookAddForm = uiMainForm.createUIComponent(UIAddBookForm.class, null, null);
      uiPopupWindow.setUIComponent(uiBookAddForm);
      uiPopupWindow.setWindowSize(500, 300);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow);

    }
  }

  public static class EditBookActionListener extends EventListener<UIBookStoreMainForm> {
    
    public void execute(Event<UIBookStoreMainForm> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      String bookId = ctx.getRequestParameter("objectId");

      UIBookStoreMainForm uiMainForm = event.getSource();
      UIPopupWindow uiPopupWindow = uiMainForm.getChild(UIPopupWindow.class);

      UIEditBookForm uiBookEditForm = uiMainForm.createUIComponent(UIEditBookForm.class, null, null);
      uiBookEditForm.setBook(uiMainForm.getBookService().getBook(bookId));
      uiBookEditForm.fillBookInfo();
      uiPopupWindow.setUIComponent(uiBookEditForm);
      uiPopupWindow.setWindowSize(500, 300);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow);

    }
  }

  public static class DeleteBookActionListener extends EventListener<UIBookStoreMainForm> {
    public void execute(Event<UIBookStoreMainForm> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      String bookId = ctx.getRequestParameter("objectId");

      UIBookStoreMainForm uiMainForm = event.getSource();
      UIPopupWindow uiPopupWindow = uiMainForm.getChild(UIPopupWindow.class);

      UIDeleteBookForm uiBookDeleteForm = uiMainForm.createUIComponent(UIDeleteBookForm.class,
                                                                     null,
                                                                     null);
      uiBookDeleteForm.setBook(uiMainForm.getBookService().getBook(bookId));
      uiBookDeleteForm.fillBookInfo();
      uiPopupWindow.setUIComponent(uiBookDeleteForm);
      uiPopupWindow.setWindowSize(500, 300);
      uiPopupWindow.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow);

    }
  }
}
