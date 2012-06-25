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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.example.entity.Book;
import org.exoplatform.example.entity.BookNodeTypes;
import org.exoplatform.example.exception.BookNotFoundException;
import org.exoplatform.example.exception.DuplicateBookException;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 29, 2012
 */
public class JCRBookAccess {
  private static final Log   log                 = ExoLogger.getLogger(JCRBookAccess.class);

  public static final String DEFAULT_PARENT_PATH = "/bookStore";

  private RepositoryService  repoService;

  public JCRBookAccess() {
  }

  void setRepositoryService(RepositoryService repoService) {
    this.repoService = repoService;
  }

  public void init() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node node = null;
    log.info("initialize JCRBookAccess");
    try {
      node = getNodeByPath(DEFAULT_PARENT_PATH, sProvider);
    } catch (PathNotFoundException e) {
      // If the path not exist then create new path
      try {
        node = getNodeByPath("/", sProvider);
        node.addNode("bookStore", BookNodeTypes.EXO_BOOK_STORE);
        node.getSession().save();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } catch (Exception e) {
      log.error("Failed to init BookStore jcr node's path", e);
    } finally {
      sProvider.close();
    }
  }

  private Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }

  private Session getSession(SessionProvider sprovider) throws Exception {
    ManageableRepository currentRepo = repoService.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(),
                                currentRepo);
  }

  private Book createBookByNode(Node bookNode) throws Exception {
    if (bookNode == null) {
      return null;
    }

    Book bookNew = new Book();
    bookNew.setId(bookNode.getName());

    bookNew.setTitle(bookNode.getProperty(BookNodeTypes.EXP_BOOK_TITLE).getString());
    bookNew.setCategory(bookNode.getProperty(BookNodeTypes.EXP_BOOK_CATEGORY).getString());
    bookNew.setDescription(bookNode.getProperty(BookNodeTypes.EXP_BOOK_DESCRIPTION).getString());
    bookNew.setPrice(bookNode.getProperty(BookNodeTypes.EXP_BOOK_PRICE).getLong());
    return bookNew;
  }

  public Book getBook(String bookid) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = getNodeByPath(DEFAULT_PARENT_PATH + "/" + bookid, sProvider);
      return createBookByNode(node);
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      log.error("Failed to get book by id", e);
      return null;
    } finally {
      sProvider.close();
    }
  }

  public Book addBook(Book book) throws DuplicateBookException {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    if (isExistBookTitle(book.getTitle(), sProvider)) {
      throw new DuplicateBookException(String.format("Book %s is existed", book.getTitle()));
    }

    String nodeId = IdGenerator.generate();
    book.setId(nodeId);

    try {
      Node parentNode = getNodeByPath(DEFAULT_PARENT_PATH, sProvider);
      Node bookNode = parentNode.addNode(nodeId, BookNodeTypes.EXO_BOOK);
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_TITLE, book.getTitle());
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_CATEGORY, book.getCategory());
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_DESCRIPTION, book.getDescription());
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_PRICE, book.getPrice());

      parentNode.getSession().save();
      return book;
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      log.error("Failed to add book", e);
      return null;
    } finally {
      sProvider.close();
    }
  }

  public void deleteBook(String id) throws BookNotFoundException {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = getNodeByPath(DEFAULT_PARENT_PATH + "/" + id, sProvider);
      node.remove();
      node.getSession().save();
    } catch (PathNotFoundException e) {
      throw new BookNotFoundException(String.format("Book %s is not found", id));
    } catch (Exception e) {
      log.error("Failed to delete book by id", e);
    } finally {
      sProvider.close();
    }
  }


  public Book editBook(Book book) throws BookNotFoundException {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = getNodeByPath(DEFAULT_PARENT_PATH + "/" + book.getId(), sProvider);
      node.setProperty(BookNodeTypes.EXP_BOOK_TITLE, book.getTitle());
      node.setProperty(BookNodeTypes.EXP_BOOK_CATEGORY, book.getCategory());
      node.setProperty(BookNodeTypes.EXP_BOOK_DESCRIPTION, book.getDescription());
      node.setProperty(BookNodeTypes.EXP_BOOK_PRICE, book.getPrice());
      node.getSession().save();
    } catch (PathNotFoundException e) {
      throw new BookNotFoundException(String.format("Book %s is not found", book.getId()));
    } catch (Exception e) {
      log.error("Failed to delete all book", e);
    } finally {
      sProvider.close();
    }
    return null;
  }

  public boolean isExistBookTitle(String bookTitle) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      return isExistBookTitle(bookTitle, sProvider);
    } finally {
      sProvider.close();
    }
  }

  private boolean isExistBookTitle(String bookTitle, SessionProvider sProvider) {
    bookTitle.replaceAll("\"", "\\\"").replaceAll("-", StringUtils.EMPTY);

    StringBuffer queryString = new StringBuffer("select * from " + BookNodeTypes.EXO_BOOK);
    queryString.append(" where " + BookNodeTypes.EXP_BOOK_TITLE + " = '" + bookTitle + "'");

    try {
      QueryManager queryManager = getSession(sProvider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator iterator = result.getNodes();
      return iterator.hasNext();
    } catch (Exception e) {
      log.error("Failed to check exist book name", e);
      return false;
    }
  }

  public List<Book> getAllBook() {
    StringBuffer queryString = new StringBuffer("select * from " + BookNodeTypes.EXO_BOOK);
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      QueryManager queryManager = getSession(sProvider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator iterator = result.getNodes();

      List<Book> books = new ArrayList<Book>();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        Book book = createBookByNode(node);
        books.add(book);
      }
      return books;
    } catch (Exception e) {
      log.error("Failed to get all book", e);
      return null;
    } finally {
      sProvider.close();
    }
  }

  public List<Book> searchTitle(String key) {
    key = key.replaceAll("\"", "\\\"").replaceAll("-", StringUtils.EMPTY);

    StringBuffer queryString = new StringBuffer("select * from " + BookNodeTypes.EXO_BOOK);
    queryString.append(" where " + BookNodeTypes.EXP_BOOK_TITLE + " like '%" + key + "%'");

    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      QueryManager queryManager = getSession(sProvider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator iterator = result.getNodes();

      List<Book> books = new ArrayList<Book>();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        Book book = createBookByNode(node);
        books.add(book);
      }
      return books;
    } catch (Exception e) {
      log.error("Failed to search book by name", e);
      return null;
    } finally {
      sProvider.close();
    }
  }
}
