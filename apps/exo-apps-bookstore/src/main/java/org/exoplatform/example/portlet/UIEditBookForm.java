package org.exoplatform.example.portlet;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.example.BookStoreService;
import org.exoplatform.example.entity.Book;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
    @EventConfig(listeners = UIEditBookForm.SaveActionListener.class),
    @EventConfig(listeners = UIEditBookForm.CancelActionListener.class) })
public class UIEditBookForm extends UIForm {

  private static final Log   log                      = ExoLogger.getLogger(UIEditBookForm.class);

  public static final String TITLE_STRING_INPUT       = "UITitleStringInput";

  public static final String DESCRIPTION_STRING_INPUT = "UIDescriptionStringInput";

  public static final String CATEGORY_STRING_INPUT    = "UICategoryStringInput";

  public static final String PRICE_STRING_INPUT       = "UIPriceStringInput";

  private Book               book;

  private BookStoreService   bookService;

  public UIEditBookForm() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    bookService = (BookStoreService) container.getComponentInstanceOfType(BookStoreService.class);
    log.info(" Exocontainer name " + container.getContext().getPortalContainerName());
    addChild(new UIFormStringInput(TITLE_STRING_INPUT, ""));
    addChild(new UIFormStringInput(DESCRIPTION_STRING_INPUT, ""));
    addChild(new UIFormStringInput(CATEGORY_STRING_INPUT, ""));
    addChild(new UIFormStringInput(PRICE_STRING_INPUT, ""));
  }

  public void setBook(Book book) {
    this.book = book;
  }

  public Book getBook() {
    return book;
  }

  public BookStoreService getBookService() {
    return bookService;
  }

  public void fillBookInfo() {
    getUIStringInput(TITLE_STRING_INPUT).setValue(book.getTitle());
    getUIStringInput(DESCRIPTION_STRING_INPUT).setValue(book.getDescription());
    getUIStringInput(CATEGORY_STRING_INPUT).setValue(book.getCategory());
    getUIStringInput(PRICE_STRING_INPUT).setValue(String.valueOf(book.getPrice()));
  }

  public static class SaveActionListener extends EventListener<UIEditBookForm> {
    public void execute(Event<UIEditBookForm> event) throws Exception {
      UIEditBookForm editForm = event.getSource();
      String title = editForm.getUIStringInput(TITLE_STRING_INPUT).getValue();
      String description = editForm.getUIStringInput(DESCRIPTION_STRING_INPUT).getValue();
      String category = editForm.getUIStringInput(CATEGORY_STRING_INPUT).getValue();
      long price = Long.parseLong(editForm.getUIStringInput(PRICE_STRING_INPUT).getValue());
      Book book = editForm.getBook();
      book.setTitle(title);
      book.setDescription(description);
      book.setCategory(category);
      book.setPrice(price);
      editForm.getBookService().editBook(book);

      // back to BookStoreView
      UIPopupWindow uiPopupWindow = editForm.getParent();
      uiPopupWindow.setShow(false);

    }

  }

  public static class CancelActionListener extends EventListener<UIEditBookForm> {
    public void execute(Event<UIEditBookForm> event) throws Exception {
      UIEditBookForm editForm = event.getSource();

      // back to BookStoreView
      UIPopupWindow uiPopupWindow = editForm.getParent();
      uiPopupWindow.setShow(false);
    }
  }
}
