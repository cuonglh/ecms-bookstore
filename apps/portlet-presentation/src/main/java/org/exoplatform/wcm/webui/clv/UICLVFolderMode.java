/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.clv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

/**
 * The Class UICLVFolderMode.
 */
@ComponentConfig(      
  lifecycle = Lifecycle.class,                 
   template = "app:/groovy/ContentListViewer/UICLVContainer.gtmpl",
   events = { 
     @EventConfig(listeners = UICLVFolderMode.QuickEditActionListener.class) 
   }
)
public class UICLVFolderMode extends UICLVContainer {

	private UICLVPresentation clvPresentation;
	
  /* (non-Javadoc)
   * @see org.exoplatform.wcm.webui.clv.UICLVContainer#init()
   */
  public void init() throws Exception {
    PortletPreferences portletPreferences = Utils.getAllPortletPreferences();    
    
    List<Node> nodes = null;
    messageKey = null;
    try {
      nodes = getRenderedContentNodes();
    } catch (ItemNotFoundException e) {
      messageKey = "UIMessageBoard.msg.folder-not-found";
      return;
    } catch (AccessDeniedException e) {
      messageKey = "UIMessageBoard.msg.no-permission";
      return;
    } catch (Exception e) {
      messageKey = "UIMessageBoard.msg.error-nodetype";
      return;
    }
    if (nodes.size() == 0) {
      messageKey = "UIMessageBoard.msg.folder-empty";
    }    
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UICLVPortlet.ITEMS_PER_PAGE, null));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(nodes, itemsPerPage);
    getChildren().clear();
    clvPresentation = addChild(UICLVPresentation.class, null, null);    
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();    
    clvPresentation.init(templatePath, resourceResolver, paginatedNodeIterator);    
    clvPresentation.setContentColumn(portletPreferences.getValue(UICLVPortlet.HEADER, null));
    clvPresentation.setShowLink(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_LINK, null)));
    clvPresentation.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_HEADER, null)));
    clvPresentation.setShowReadmore(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_READMORE, null)));
    clvPresentation.setHeader(portletPreferences.getValue(UICLVPortlet.HEADER, null));
  }
  
  /**
   * Gets the rendered content nodes.
   * 
   * @return the rendered content nodes
   * 
   * @throws Exception the exception
   */
  public List<Node> getRenderedContentNodes() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();      
    WCMComposer wcmComposer = getApplicationComponent(WCMComposer.class);
    HashMap<String, String> filters = new HashMap<String, String>();
    filters.put(WCMComposer.FILTER_MODE, Utils.getCurrentMode());
    String orderBy = preferences.getValue(UICLVPortlet.ORDER_BY, null);
    String orderType = preferences.getValue(UICLVPortlet.ORDER_TYPE, null);
    if (orderType == null) orderType = "DESC";
    if (orderBy == null) orderBy = "exo:title";
    filters.put(WCMComposer.FILTER_ORDER_BY, orderBy);
    filters.put(WCMComposer.FILTER_ORDER_TYPE, orderType);
    filters.put(WCMComposer.FILTER_LANGUAGE, Util.getPortalRequestContext().getLocale().getLanguage());
    
    if(preferences.getValue(UICLVPortlet.FOLDER_PATH, null) == null){
        return new ArrayList<Node>();
    }      
    NodeLocation nodeLocation = NodeLocation.getNodeLocationByExpression(preferences.getValue(UICLVPortlet.FOLDER_PATH, null));
    return wcmComposer.getContents(nodeLocation.getRepository(), nodeLocation.getWorkspace(), nodeLocation.getPath(), filters, WCMCoreUtils.getUserSessionProvider());
  }
}