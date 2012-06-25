/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.example;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.example.entity.Book;
import org.exoplatform.example.exception.BookNotFoundException;
import org.exoplatform.example.exception.DuplicateBookException;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 29, 2012
 */
public class BookStoreServiceImpl implements BookStoreService {


  private static final Log log           = ExoLogger.getLogger(BookStoreServiceImpl.class);

  private JCRBookAccess    jcrBookAccess = new JCRBookAccess();

  public BookStoreServiceImpl() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    jcrBookAccess.setRepositoryService((RepositoryService) container.getComponentInstanceOfType(RepositoryService.class));
    jcrBookAccess.init();
  }

  public Book getBook(String id) {
    return jcrBookAccess.getBook(id);
  }

  public Book addBook(String category, String bookTitle, String description, long price) throws DuplicateBookException {
    Book book = new Book( category, bookTitle, description, price);
    return jcrBookAccess.addBook(book);
  }

  public void deleteBook(String id) throws BookNotFoundException {
    jcrBookAccess.deleteBook(id);
  }


  public Book editBook(Book book) throws BookNotFoundException {
    return jcrBookAccess.editBook(book);
  }

  public List<Book> searchTitle(String key) {
    return jcrBookAccess.searchTitle(key);
  }

  public List<Book> getAll() {
    return jcrBookAccess.getAllBook();
  }

  public boolean isExistBookTitle(String bookTitle) {
    return jcrBookAccess.isExistBookTitle(bookTitle);
  }

}
