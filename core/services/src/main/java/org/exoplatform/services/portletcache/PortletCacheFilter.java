/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.services.portletcache;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletCacheFilter implements PortletFilter, ActionFilter, RenderFilter, EventFilter
{

  public PortletCacheFilter()
  {
  }

  public void init(FilterConfig cfg) throws PortletException
  {
  }

  public void destroy()
  {
  }

  public void doFilter(ActionRequest req, ActionResponse resp, FilterChain chain) throws IOException, PortletException
  {
    chain.doFilter(req, resp);
  }

  public void doFilter(EventRequest req, EventResponse resp, FilterChain chain) throws IOException, PortletException
  {
    chain.doFilter(req, resp);
  }

  public void doFilter(RenderRequest req, RenderResponse resp, FilterChain chain) throws IOException, PortletException
  {
    if (req.getRemoteUser() == null)
    {
      PortalRequestContext ctx = (PortalRequestContext)PortalRequestContext.getCurrentInstance();
      Map<String, String[]> query = (Map<String, String[]>)ctx.getRequest().getParameterMap();

      //
      Locale locale = ctx.getLocale();

      //
      WindowKey key = new WindowKey(
          req.getWindowID(),
          req.getWindowState(),
          req.getPortletMode(),
          locale,
          req.getParameterMap(),
          query);

      //
      FragmentCacheService service = (FragmentCacheService)PortalContainer.getInstance().getComponentInstanceOfType(FragmentCacheService.class);
      MarkupFragment value = service.cache.get(new PortletRenderContext(req, resp, chain), key);
      OutputStream out = resp.getPortletOutputStream();
      out.write(value.data);
      out.close();
    }
    else
    {
      chain.doFilter(req, resp);
    }
  }
}
