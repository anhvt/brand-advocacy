/**
 * Created by exoplatform on 01/10/14.
 */

@Application(defaultController = org.exoplatform.community.brandadvocacy.portlet.backend.JuZBackEndApplication.class)
@WebJars(@WebJar("jquery"))
@Portlet(name="BackendPortlet")
@Bindings(
  {
    @Binding(value = org.exoplatform.services.organization.OrganizationService.class),
    @Binding(value = org.exoplatform.social.core.manager.IdentityManager.class),
    @Binding(value = org.exoplatform.brandadvocacy.service.IService.class),
    @Binding(LoginController.class)
  }
)
@Scripts({
   @Script(id = "jquery", value = "jquery/1.7.2/jquery.js"),
//   @Script(id = "jquery-full", value = "js/jquery.1.7.2.min.js",location = AssetLocation.SERVER),
   @Script(
    id = "ckeditor", value = "js/ckeditor/ckeditor.js",location = AssetLocation.SERVER,depends = {"jquery"}
   ),
   @Script(
   id = "ckeditorAdapters", value = "js/ckeditor/adapters/jquery.js",location = AssetLocation.SERVER,depends = {"jquery"}
   ),
   @Script(
    id = "bradBKJS", value = "js/brad-backend.js",location = AssetLocation.SERVER,depends = {"jquery"}
    ),
   @Script(id="iphonePlugin", value="js/iphone-style-checkboxes.js",location = AssetLocation.SERVER,depends = {"jquery"})

})
@Stylesheets(
{
  @Stylesheet(value = "css/brad-backend.css",location = AssetLocation.SERVER)
}
)

@Assets("*")
package org.exoplatform.community.brandadvocacy.portlet.backend;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.*;
import juzu.plugin.servlet.Servlet;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;
import juzu.Application;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import org.exoplatform.community.brandadvocacy.portlet.backend.controllers.LoginController;