/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wcm.connector.fckeditor;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong_phan@exoplatform.com
 * Jan 21, 2009
 */
@Path("/wcmGadget/")
public class GadgetConnector implements ResourceContainer {
  
  /** The Constant FCK_RESOURCE_BUNDLE_FILE. */
  public static final String FCK_RESOURCE_BUNDLE_FILE   = "locale.services.fckeditor.FCKConnector".intern();
  
  /** The application registry service. */
  private ApplicationRegistryService applicationRegistryService;
  
  /** The gadget registry service. */
  private GadgetRegistryService gadgetRegistryService;
  
  /** The internal server path. */
  private String internalServerPath;
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(GadgetConnector.class);
  
  /**
   * Instantiates a new gadget connector.
   * 
   * @param container the container
   * @param initParams the init params
   */
  public GadgetConnector(InitParams initParams) {
    applicationRegistryService = WCMCoreUtils.getService(ApplicationRegistryService.class);
    gadgetRegistryService = WCMCoreUtils.getService(GadgetRegistryService.class);
    readServerConfig(initParams);
  }
  
  /**
   * Read server config.
   * 
   * @param initParams the init params
   */
  private void readServerConfig(InitParams initParams) {
    PropertiesParam propertiesParam = initParams.getPropertiesParam("server.config");
    String scheme = propertiesParam.getProperty("scheme");
    String hostName = propertiesParam.getProperty("hostName");
    String port = propertiesParam.getProperty("port");
    StringBuilder builder = new StringBuilder();
    builder.append(scheme).append("://").append(hostName).append(":").append(port);
    internalServerPath = builder.toString();
  }
  
  /**
   * Gets the folders and files.
   * 
   * @param currentFolder the current folder
   * @param language the language
   * 
   * @return the folders and files
   * 
   * @throws Exception the exception
   */
  @GET
  @Path("/getFoldersAndFiles/")
//  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("currentFolder") String currentFolder, @QueryParam("currentFolder") String language) throws Exception {
    try {
      Response response = buildXMLResponse(currentFolder, language);
      if (response != null)
        return response; 
    } catch (Exception e) {
      log.error("Error when perform getFoldersAndFiles: ", e.fillInStackTrace());
    }    
    return Response.ok().build();
  }
  
  /**
   * Builds the xml response.
   * 
   * @param currentFolder the current folder
   * @param language the language
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  public Response buildXMLResponse(String currentFolder, String language) throws Exception {
    List<ApplicationCategory> applicationCategories = getGadgetCategories();
    Element rootElement = createRootElement(currentFolder, applicationCategories, language);
    Document document = rootElement.getOwnerDocument();
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    return Response.ok(new DOMSource(document), MediaType.TEXT_XML).cacheControl(cacheControl).build();
  }
  
  /**
   * Creates the root element.
   * 
   * @param currentFolder the current folder
   * @param applicationCategories the application categories
   * @param language the language
   * 
   * @return the element
   * 
   * @throws Exception the exception
   */
  private Element createRootElement(String currentFolder, List<ApplicationCategory> applicationCategories, String language) throws Exception {
    Document document = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.newDocument();
    if (applicationCategories.isEmpty()) {
      Locale locale = null;
      if (language == null) {
        locale = Locale.ENGLISH;
      } else {
        locale = new Locale(language);
      }
      ResourceBundle resourceBundle = ResourceBundle.getBundle(FCK_RESOURCE_BUNDLE_FILE, locale);
      String message = "";
      try {
				message = resourceBundle.getString("fckeditor.no-gadget");
			} catch (MissingResourceException e) {
				message = "fckeditor.no-gadget";
			}
      Element rootElement = document.createElement("Message");
      document.appendChild(rootElement);
      rootElement.setAttribute("number", "555");
      rootElement.setAttribute("text", message);
      rootElement.setAttribute("type", "Error");
      return rootElement;
    } else {
      Element rootElement = document.createElement("Connector");
      document.appendChild(rootElement);
      rootElement.setAttribute("resourceType", "Gadget");    
      Element currentFolderElement = document.createElement("CurrentFolder");
      if (currentFolder == null || currentFolder.equals("/")){
        currentFolderElement.setAttribute("name", applicationCategories.get(0).getName());
        Element foldersElement = createFolderElement(document, applicationCategories);
        rootElement.appendChild(foldersElement);
      } else {
        ApplicationCategory applicationCategory = applicationRegistryService.getApplicationCategory(currentFolder.substring(1, currentFolder.length() - 1));
        currentFolderElement.setAttribute("name", applicationCategory.getDisplayName());
        Element filesElement = createFileElement(document, applicationCategory);
        rootElement.appendChild(filesElement);
      }
      rootElement.appendChild(currentFolderElement);
      return rootElement;
    }
  }
  
  /**
   * Creates the folder element.
   * 
   * @param document the document
   * @param applicationCategories the application categories
   * 
   * @return the element
   * 
   * @throws Exception the exception
   */
  private Element createFolderElement(Document document, List<ApplicationCategory> applicationCategories) throws Exception {
    Element folders = document.createElement("Folders");
    for (ApplicationCategory applicationCategory : applicationCategories) {
      Element folder = document.createElement("Folder");
      folder.setAttribute("name", applicationCategory.getDisplayName());
      folders.appendChild(folder);  
    }
    return folders;
  }
  
  /**
   * Creates the file element.
   * 
   * @param document the document
   * @param applicationCategory the application category
   * 
   * @return the element
   * 
   * @throws Exception the exception
   */
  private Element createFileElement(Document document, ApplicationCategory applicationCategory) throws Exception {
    Element files = document.createElement("Files");
    List<Application> listApplication = applicationRegistryService.getApplications(applicationCategory, ApplicationType.GADGET);
    for (Application application : listApplication) {
      Gadget gadget = gadgetRegistryService.getGadget(application.getApplicationName());
      Element file = document.createElement("File");
      file.setAttribute("name", gadget.getName());
      file.setAttribute("fileType", "nt_unstructured");
      file.setAttribute("size", "0");
      file.setAttribute("thumbnail", gadget.getThumbnail());
      file.setAttribute("description", gadget.getDescription());
      
      String fullurl = "";
      if (gadget.isLocal()) {
        fullurl = internalServerPath + "/rest/" + gadget.getUrl();
      } else {
        fullurl = gadget.getUrl();
      }
      file.setAttribute("url", fullurl);
      
      String data = "{\"context\":{\"country\":\"US\",\"language\":\"en\"},\"gadgets\":[{\"moduleId\":0,\"url\":\"" + fullurl + "\",\"prefs\":[]}]}";
      URL url = new URL(internalServerPath + "/eXoGadgetServer/gadgets/metadata");
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();
      String strMetadata = IOUtils.toString(conn.getInputStream(), "UTF-8");
      wr.close();
      JSONObject metadata = new JSONObject(strMetadata.toString());
      file.setAttribute("metadata", metadata.toString());
      files.appendChild(file);
    }
    return files;
  }
  
  /**
   * Gets the gadget categories.
   * 
   * @return the gadget categories
   * 
   * @throws Exception the exception
   */
  private List<ApplicationCategory> getGadgetCategories() throws Exception {
    List<ApplicationCategory> applicationCategories = applicationRegistryService.getApplicationCategories();
    List<ApplicationCategory> gadgetCategories = new ArrayList<ApplicationCategory>();
    for (ApplicationCategory applicationCategory : applicationCategories) {
      if (!applicationRegistryService.getApplications(applicationCategory, ApplicationType.GADGET).isEmpty()) {
        gadgetCategories.add(applicationCategory);
      }
    }
    return gadgetCategories;
  }
}