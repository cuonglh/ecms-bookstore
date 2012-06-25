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

import org.exoplatform.example.entity.Book;
import org.exoplatform.example.exception.BookNotFoundException;
import org.exoplatform.example.exception.DuplicateBookException;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 29, 2012
 */
public interface BookStoreService {

  /**
   * 
   * @param id
   * @return
   */
  public Book getBook(String id);

  /**
   * 
   * @param category
   * @param bookTitle
   * @param description
   * @param price
   * @return
   * @throws DuplicateBookException
   */
  public Book addBook(String category, String bookTitle, String description, long price) throws DuplicateBookException;

  /**
   * 
   * @param id
   * @throws BookNotFoundException
   */
  public void deleteBook(String id) throws BookNotFoundException;
  
  /**
   * 
   * @param book
   * @return
   * @throws BookNotFoundException
   */
  public Book editBook(Book book) throws BookNotFoundException;

  public List<Book> searchTitle(String key);

  public List<Book> getAll();

  public boolean isExistBookTitle(String bookTitle);
}
