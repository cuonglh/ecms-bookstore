/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.component.explorer.search;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.portlet.PortletRequest;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.presentation.AbstractActionComponent;
import org.exoplatform.ecm.webui.presentation.NodePresentation;
import org.exoplatform.ecm.webui.presentation.removeattach.RemoveAttachmentComponent;
import org.exoplatform.ecm.webui.presentation.removecomment.RemoveCommentComponent;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Apr 6, 2007 4:21:18 PM
 */
@ComponentConfig(
    events = {
        @EventConfig(listeners = UIViewSearchResult.ChangeLanguageActionListener.class),
        @EventConfig(listeners = UIViewSearchResult.DownloadActionListener.class),
        @EventConfig(listeners = UIViewSearchResult.ChangeNodeActionListener.class)
    }
)
public class UIViewSearchResult extends UIContainer implements NodePresentation {
  
  private Node node_ ;
  private String language_ ;
  final private static String COMMENT_COMPONENT = "Comment".intern();
  /**
   * Logger.
   */
  private static final Log LOG  = ExoLogger.getLogger("cms.UIViewSearchResult");
  
  public UIViewSearchResult() throws Exception {
  }

  
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class) ;
    try {
      String nodeType = node_.getPrimaryNodeType().getName() ;
      return templateService.getTemplatePathByUser(false, nodeType, userName, uiExplorer.getRepositoryName()) ; 
    } catch(Exception e) {
      LOG.error(e);
    }
    return null; 
  }
  
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    NodeIterator childrenIterator = node_.getNodes();;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    int attachData = 0 ;
    while(childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      List<String> listCanCreateNodeType = 
        Utils.getListAllowedFileType(node_, getRepository(), templateService) ;       
      if(listCanCreateNodeType.contains(nodeType)) {
        
        // Case of childNode has jcr:data property
        if (childNode.hasProperty(Utils.JCR_DATA)) {
          attachData = childNode.getProperty(Utils.JCR_DATA).getStream().available();
          
          // Case of jcr:data has content.
          if (attachData > 0) 
            attachments.add(childNode);          
        } else {
          attachments.add(childNode);
        }               
      }
    }
    return attachments;
  }

  public UIComponent getRemoveAttach() throws Exception {
    removeChild(RemoveAttachmentComponent.class);
    UIComponent uicomponent = addChild(RemoveAttachmentComponent.class, null, "UIViewSearchResultRemoveAttach");
    ((AbstractActionComponent)uicomponent).setLstComponentupdate(Arrays.asList(new Class[] {UIPopupWindow.class}));
    return uicomponent;
  }

  public UIComponent getRemoveComment() throws Exception {
    removeChild(RemoveCommentComponent.class);
    UIComponent uicomponent = addChild(RemoveCommentComponent.class, null, "UIViewSearchResultRemoveComment");
    ((AbstractActionComponent)uicomponent).setLstComponentupdate(Arrays.asList(new Class[] {UIPopupWindow.class}));
    return uicomponent;
  }
  
  public Node getNode() throws ValueFormatException, PathNotFoundException, RepositoryException { 
    if(node_.hasProperty(Utils.EXO_LANGUAGE)) {
      String defaultLang = node_.getProperty(Utils.EXO_LANGUAGE).getString() ;
      if(language_ == null) language_ =  defaultLang ;
      if(node_.hasNode(Utils.LANGUAGES)) {
        if(!language_.equals(defaultLang)) {
          Node curNode = node_.getNode(Utils.LANGUAGES + Utils.SLASH + language_) ;
          return curNode ;
        } 
      }
      return node_ ;
    }    
    return node_ ; 
  }
  public Node getOriginalNode() throws Exception {return node_;}
  
  public String getIcons(Node node, String size) throws Exception {
    return Utils.getNodeTypeIcon(node, size) ;
  }
  
  public String getNodeType() throws Exception { return null; }
  
  public List<Node> getRelations() throws Exception {
    List<Node> relations = new ArrayList<Node>() ;
    if (node_.hasProperty(Utils.EXO_RELATION)) {
      Value[] vals = node_.getProperty(Utils.EXO_RELATION).getValues();
      for (int i = 0; i < vals.length; i++) {
        String uuid = vals[i].getString();
        Node node = getNodeByUUID(uuid);
        relations.add(node);
      }
    }
    return relations;
  }

  public boolean isRssLink() { return false ; }
  public String getRssLink() { return null ; }

  public List getSupportedLocalise() throws Exception {
    MultiLanguageService multiLanguageService = getApplicationComponent(MultiLanguageService.class) ;
    return multiLanguageService.getSupportedLanguages(node_) ;
  }

  public String getTemplatePath() throws Exception { return null; }

  public boolean isNodeTypeSupported() { return false; }
  
  public boolean isNodeTypeSupported(String nodeTypeName) {
    try {      
      TemplateService templateService = getApplicationComponent(TemplateService.class);
      String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
      return templateService.isManagedNodeType(nodeTypeName, repository);
    } catch (Exception e) {
      return false;
    }
  }
  
  public UIComponent getCommentComponent() {
    UIComponent uicomponent = null;
    try {
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      Map<String, Object> context = new HashMap<String, Object>();
      UIJCRExplorer uiExplorer = getAncestorOfType(UIJCRExplorer.class);
      context.put(UIJCRExplorer.class.getName(), uiExplorer);
      context.put(Node.class.getName(), node_);
      uicomponent = manager.addUIExtension(ManageViewService.EXTENSION_TYPE, COMMENT_COMPONENT, context, this);
    } catch (Exception e) {
      LOG.error("An error occurs while checking the action", e);
    }
    return (uicomponent != null ? uicomponent : this);
  }
  
  public boolean hasPropertyContent(Node node, String property) {
    try {
      String value = node.getProperty(property).getString() ;
      if(value.length() > 0) return true ;
    } catch (Exception e) {
      LOG.error(e);      
    }
    return false ;
  }

  public void setNode(Node node) { node_ = node ; }
  
  public Node getNodeByUUID(String uuid) throws Exception{
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
    ManageableRepository manageRepo = getApplicationComponent(RepositoryService.class).getRepository(repository) ;
    String[] workspaces = manageRepo.getWorkspaceNames() ;
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;
    for(String ws : workspaces) {
      try{
        return sessionProvider.getSession(ws,manageRepo).getNodeByUUID(uuid) ;
      }catch(Exception e) {
        
      }      
    }
    return null;
  }
  
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return getAncestorOfType(UIJCRExplorer.class).getJCRTemplateResourceResolver() ;
  }

  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(node_, language_) ;
  }
  
  public String getViewTemplate(String nodeTypeName, String templateName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    String repository = getAncestorOfType(UIJCRExplorer.class).getRepositoryName() ;
    return tempServ.getTemplatePath(false, nodeTypeName, templateName, repository) ;
  }

  public String getLanguage() { return language_; }

  public void setLanguage(String language) { language_ = language ; }

  @SuppressWarnings("unchecked")
  public Object getComponentInstanceOfType(String className) {
    Object service = null;
    try {
      ClassLoader loader =  Thread.currentThread().getContextClassLoader();
      Class clazz = loader.loadClass(className);
      service = getApplicationComponent(clazz);
    } catch (ClassNotFoundException ex) {
      LOG.error(ex);
    } 
    return service;
  }
  

  public String getImage(Node node) throws Exception {
    DownloadService downloadService = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource inputResource ;
    Node imageNode = node.getNode(Utils.EXO_IMAGE) ;
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    inputResource = new InputStreamDownloadResource(input, "image") ;
    inputResource.setDownloadName(node.getName()) ;
    return downloadService.getDownloadLink(downloadService.addDownloadResource(inputResource)) ;
  }
  
  public String getImage(Node node, String nodeTypeName) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    Node imageNode = node.getNode(nodeTypeName) ;    
    InputStream input = imageNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String getPortalName() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PortalContainerInfo containerInfo = (PortalContainerInfo)container.getComponentInstanceOfType(PortalContainerInfo.class);      
    return containerInfo.getContainerName();
  }
  
  public String getRepository() throws Exception {
    return ((ManageableRepository)node_.getSession().getRepository()).getConfiguration().getName() ;
  }
  
  public String getWebDAVServerPrefix() throws Exception {
    PortletRequestContext pRequestContext = PortletRequestContext.getCurrentInstance() ;
    PortletRequest pRequest = pRequestContext.getRequest() ;
    String prefixWebDAV = pRequest.getScheme() + "://" + pRequest.getServerName() + ":" 
                          + String.format("%s",pRequest.getServerPort()) ;
    return prefixWebDAV ;
  }

  public String getWorkspaceName() throws Exception {
    return node_.getSession().getWorkspace().getName() ;
  }
  static public class ChangeLanguageActionListener extends EventListener<UIViewSearchResult> {
    public void execute(Event<UIViewSearchResult> event) throws Exception {
      UIViewSearchResult uiViewSearchResult = event.getSource() ;
      String selectedLanguage = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiViewSearchResult.setLanguage(selectedLanguage) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewSearchResult.getParent()) ;
    }   
  }
  public String getDownloadLink(Node node) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    if(!node.getPrimaryNodeType().getName().equals(Utils.NT_FILE)) return null; 
    Node jcrContentNode = node.getNode(Utils.JCR_CONTENT) ;
    InputStream input = jcrContentNode.getProperty(Utils.JCR_DATA).getStream() ;
    dresource = new InputStreamDownloadResource(input, "image") ;
    dresource.setDownloadName(node.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String encodeHTML(String text) throws Exception {
    return Utils.encodeHTML(text) ;
  }
 
  public String getTemplateSkin(String nodeTypeName, String skinName) throws Exception {
    TemplateService tempServ = getApplicationComponent(TemplateService.class) ;
    return tempServ.getSkinPath(nodeTypeName, skinName, getLanguage(), getRepository()) ;
  }


  public UIComponent getUIComponent(String mimeType) throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    List<UIExtension> extensions = manager.getUIExtensions(Utils.FILE_VIEWER_EXTENSION_TYPE);
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(Utils.MIME_TYPE, mimeType);
    for (UIExtension extension : extensions) {
      UIComponent uiComponent = manager.addUIExtension(extension, context, this);
      if(uiComponent != null) return uiComponent;
    }
    return null;
  }

  
  private Node getFileLangNode(Node currentNode) throws Exception {
    if(currentNode.getNodes().getSize() > 0) {
      NodeIterator nodeIter = currentNode.getNodes() ;
      while(nodeIter.hasNext()) {
        Node ntFile = nodeIter.nextNode() ;
        if(ntFile.getPrimaryNodeType().getName().equals("nt:file")) {
          return ntFile ;
        }
      }
      return currentNode ;
    }
    return currentNode ;
  } 
  
  static  public class DownloadActionListener extends EventListener<UIViewSearchResult> {
    public void execute(Event<UIViewSearchResult> event) throws Exception {
      UIViewSearchResult uiComp = event.getSource() ;
      String downloadLink = uiComp.getDownloadLink(uiComp.getFileLangNode(uiComp.getNode()));
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComp.getParent()) ;
    }
  }
  
  static  public class ChangeNodeActionListener extends EventListener<UIViewSearchResult> {
    public void execute(Event<UIViewSearchResult> event) throws Exception {
      UIViewSearchResult uicomp =  event.getSource() ;
      UIJCRExplorer uiExplorer = uicomp.getAncestorOfType(UIJCRExplorer.class) ; 
      String uri = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String workspaceName = event.getRequestContext().getRequestParameter("workspaceName") ;
      Session session = uiExplorer.getSessionByWorkspace(workspaceName) ;
      Node selectedNode = (Node) session.getItem(uri) ;
      uicomp.setNode(selectedNode) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uicomp.getParent()) ;
    }
  }
  
  public boolean isEnableComment() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isEnableVote() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setEnableComment(boolean value) {
    // TODO Auto-generated method stub
  }

  public void setEnableVote(boolean value) {
    // TODO Auto-generated method stub
  }

}