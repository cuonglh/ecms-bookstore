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
    @EventConfig(listeners = UIDeleteBookForm.DeleteActionListener.class),
    @EventConfig(listeners = UIDeleteBookForm.CancelActionListener.class) })
public class UIDeleteBookForm extends UIForm {

  private static final Log   log                      = ExoLogger.getLogger(UIDeleteBookForm.class);

  public static final String TITLE_STRING_INPUT       = "UITitleStringInput";

  public static final String DESCRIPTION_STRING_INPUT = "UIDescriptionStringInput";

  public static final String CATEGORY_STRING_INPUT    = "UICategoryStringInput";

  public static final String PRICE_STRING_INPUT       = "UIPriceStringInput";

  private Book book;
  
  private BookStoreService   bookService;

  public UIDeleteBookForm() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    log.info(" Exocontainer name "+container.getContext().getPortalContainerName());
    bookService = (BookStoreService) container.getComponentInstanceOfType(BookStoreService.class);
    UIFormStringInput titleInput = new UIFormStringInput(TITLE_STRING_INPUT,"");
    titleInput.setDisabled(true);
    addChild(titleInput);
    
    UIFormStringInput descriptionInput= new UIFormStringInput(DESCRIPTION_STRING_INPUT,"");
    descriptionInput.setDisabled(true);
    addChild(descriptionInput);
    
    UIFormStringInput categoryInput= new UIFormStringInput(CATEGORY_STRING_INPUT,"");
    categoryInput.setDisabled(true);
    addChild(categoryInput);
    
    UIFormStringInput priceInput= new UIFormStringInput(PRICE_STRING_INPUT,"");
    priceInput.setDisabled(true);
    addChild(priceInput);
  }

  public BookStoreService getBookService() {
    return bookService;
  }


  public void setBook(Book book) {
    this.book = book;
  }

  public Book getBook() {
    return book;
  }
  
  public void fillBookInfo() {
    getUIStringInput(TITLE_STRING_INPUT).setValue(book.getTitle());
    getUIStringInput(DESCRIPTION_STRING_INPUT).setValue(book.getDescription());
    getUIStringInput(CATEGORY_STRING_INPUT).setValue(book.getCategory());
    getUIStringInput(PRICE_STRING_INPUT).setValue(String.valueOf(book.getPrice()));
  }
  
  public static class DeleteActionListener extends EventListener<UIDeleteBookForm> {
    public void execute(Event<UIDeleteBookForm> event) throws Exception {
      UIDeleteBookForm deleteForm = event.getSource();
      Book book =deleteForm.getBook();
      deleteForm.getBookService().deleteBook(book.getId());
      
      //back to BookStoreView
      UIPopupWindow uiPopupWindow = deleteForm.getParent();
      uiPopupWindow.setShow(false);
      
    }
    
  }

  public static class CancelActionListener extends EventListener<UIDeleteBookForm> {
    public void execute(Event<UIDeleteBookForm> event) throws Exception {
      UIDeleteBookForm deleteForm = event.getSource();
      
      //back to BookStoreView
      UIPopupWindow uiPopupWindow = deleteForm.getParent();
      uiPopupWindow.setShow(false);
    }
  }
}
