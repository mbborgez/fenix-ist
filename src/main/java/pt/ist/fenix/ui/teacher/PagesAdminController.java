package pt.ist.fenix.ui.teacher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Professorship;
import net.sourceforge.fenixedu.injectionCode.AccessControl;
import net.sourceforge.fenixedu.presentationTier.Action.teacher.ManageExecutionCourseDA;
import org.fenixedu.bennu.io.domain.GroupBasedFile;
import org.fenixedu.cms.domain.MenuItem;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;
import org.fenixedu.core.ui.StrutsFunctionalityController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Optional;

import static pt.ist.fenixframework.FenixFramework.getDomainObject;

@RestController
@RequestMapping("/teacher/{executionCourseId}/pages")
public class PagesAdminController extends StrutsFunctionalityController {

    @Autowired
    PagesAdminService service;

    @RequestMapping(method = RequestMethod.GET)
    public PagesAdminView all(Model model, @PathVariable String executionCourseId) {
        ExecutionCourse executionCourse = executionCourse(executionCourseId);
        Professorship professorship = executionCourse.getProfessorship(AccessControl.getPerson());
        AccessControl.check(person -> professorship!=null && professorship.getPermissions().getSections());
        model.addAttribute("executionCourse", executionCourse);
        model.addAttribute("professorship", professorship);
        return PagesAdminView.getInstance();
    }

    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String data(@PathVariable String executionCourseId) {
        return service.serialize(executionCourse(executionCourseId).getCmsSite()).toString();
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String create(@PathVariable String executionCourseId, @RequestBody String bodyJson) {
        PagesAdminBean bean = new PagesAdminBean(bodyJson);
        ExecutionCourseSite site = executionCourse(executionCourseId).getCmsSite();
        Optional<MenuItem> menuItem = service.create(site, bean.getParent(), bean.getTitle(), bean.getBody(), bean.getPosition());
        return service.serialize(menuItem.get()).toString();
    }

    @RequestMapping(value = "/{menuItemId}", method = RequestMethod.DELETE)
    public @ResponseBody String delete(@PathVariable String executionCourseId, @PathVariable String menuItemId) {
        service.delete(getDomainObject(menuItemId));
        return data(executionCourseId);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String edit(@RequestBody String bodyJson) {
        PagesAdminBean bean = new PagesAdminBean(bodyJson);
        MenuItem menuItem = service.edit(bean.getMenuItem(), bean.getParent(), bean.getTitle(), bean.getBody(), bean.getPosition(), bean.getCanViewGroup());
        return service.serialize(menuItem).toString();
    }

    @RequestMapping(value = "/attachment", method = RequestMethod.POST)
    public RedirectView addAttachments(@PathVariable String executionCourseId,
                             @RequestParam(required = true) String menuItemId,
                             @RequestParam(required = true) String name,
                             @RequestParam("attachment") MultipartFile attachment) throws IOException {
        service.addAttachment(name, attachment, getDomainObject(menuItemId));
        return new RedirectView(String.format("/teacher/%s/pages#%s", executionCourseId, menuItemId), true);
    }

    @RequestMapping(value = "/attachment/{menuItemId}/{fileId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String deleteAttachments(@PathVariable String menuItemId, @PathVariable String fileId) {
        MenuItem menuItem = getDomainObject(menuItemId);
        GroupBasedFile postFile = getDomainObject(fileId);
        service.delete(menuItem, postFile);
        return getAttachments(menuItemId);
    }

    @RequestMapping(value = "/attachments", method = RequestMethod.GET)
    public @ResponseBody String getAttachments(@RequestParam(required = true) String menuItemId) {
        MenuItem menuItem = getDomainObject(menuItemId);
        return service.serializeAttachments(menuItem.getPage()).toString();
    }

    @RequestMapping(value = "/attachment", method = RequestMethod.PUT)
    public @ResponseBody String updateAttachment(@RequestBody String bodyJson) {
        JsonObject updateMessage = new JsonParser().parse(bodyJson).getAsJsonObject();
        MenuItem menuItem = getDomainObject(updateMessage.get("menuItemId").getAsString());
        GroupBasedFile attachment = getDomainObject(updateMessage.get("fileId").getAsString());
        service.updateAttachment(menuItem, attachment, updateMessage.get("position").getAsInt());
        return getAttachments(menuItem.getExternalId());
    }

    @Override
    protected Class<?> getFunctionalityType() {
        return ManageExecutionCourseDA.class;
    }

    protected static ExecutionCourse executionCourse(String executionCourseId) {
        return getDomainObject(executionCourseId);
    }


}
