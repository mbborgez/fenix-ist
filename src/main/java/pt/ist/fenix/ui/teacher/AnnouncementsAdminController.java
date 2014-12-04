package pt.ist.fenix.ui.teacher;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Professorship;
import net.sourceforge.fenixedu.injectionCode.AccessControl;
import net.sourceforge.fenixedu.presentationTier.Action.teacher.ManageExecutionCourseDA;
import org.fenixedu.cms.domain.Category;
import org.fenixedu.cms.domain.Post;
import org.fenixedu.cms.domain.Site;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.core.ui.StrutsFunctionalityController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.bennu.core.security.Authenticate.getUser;
import static org.fenixedu.cms.domain.Post.CREATION_DATE_COMPARATOR;
import static pt.ist.fenixframework.FenixFramework.atomic;
import static pt.ist.fenixframework.FenixFramework.getDomainObject;

@Controller
@RequestMapping("/teacher/{executionCourseId}/announcements")
public class AnnouncementsAdminController extends StrutsFunctionalityController {
    private static final LocalizedString ANNOUNCEMENT = getLocalizedString("resources.FenixEduCMSResources", "label.announcements");

    @RequestMapping(method = RequestMethod.GET)
    public AnnouncementsAdminView all(Model model, @PathVariable String executionCourseId){
        ExecutionCourse executionCourse = getDomainObject(executionCourseId);
        Professorship professorship = executionCourse.getProfessorship(AccessControl.getPerson());
        AccessControl.check(person -> professorship!=null && professorship.getPermissions().getAnnouncements());
        model.addAttribute("executionCourse", executionCourse);
        model.addAttribute("announcements", getAnnouncements(executionCourse.getCmsSite()));
        model.addAttribute("professorship", professorship);
        return AnnouncementsAdminView.getInstance();
    }

    @RequestMapping(value = "{postSlug}/delete", method = RequestMethod.POST)
    public RedirectView delete(@PathVariable String executionCourseId, @PathVariable String postSlug) {
        ExecutionCourse executionCourse = getDomainObject(executionCourseId);
        Post post = executionCourse.getCmsSite().postForSlug(postSlug);
        atomic(() -> post.delete());
        return viewAll(executionCourse);
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public RedirectView create(@PathVariable String executionCourseId, @RequestParam LocalizedString name,
                               @RequestParam LocalizedString body) throws Exception {
        ExecutionCourse executionCourse = getDomainObject(executionCourseId);
        Site cmsSite = executionCourse.getCmsSite();
        atomic(() -> Post.create(cmsSite, null, name, body, announcementsCategory(cmsSite), true, getUser()));
        return viewAll(executionCourse);
    }

    @RequestMapping(value = "{postSlug}/edit", method = RequestMethod.POST)
    public RedirectView edit(@PathVariable String executionCourseId, @PathVariable String postSlug,
                             @RequestParam LocalizedString name, @RequestParam LocalizedString body) {
        ExecutionCourse executionCourse = getDomainObject(executionCourseId);
        Post post = executionCourse.getCmsSite().postForSlug(postSlug);
        atomic(() -> {
            post.setName(name);
            post.setBody(body);
        });
        return viewAll(executionCourse);
    }

    private RedirectView viewAll(ExecutionCourse executionCourse) {
        return new RedirectView(format("/teacher/%s/announcements", executionCourse.getExternalId()), true);
    }

    private Collection<Post> getAnnouncements(Site cmsSite) {
        return announcementsCategory(cmsSite).getPostsSet().stream().sorted(CREATION_DATE_COMPARATOR).collect(toList());
    }

    private Category announcementsCategory(Site cmsSite) {
        return cmsSite.categoryForSlug("announcement", ANNOUNCEMENT);
    }

    @Override
    protected Class<?> getFunctionalityType() {
        return ManageExecutionCourseDA.class;
    }

}
