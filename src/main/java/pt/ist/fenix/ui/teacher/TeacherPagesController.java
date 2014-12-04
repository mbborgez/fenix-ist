package pt.ist.fenix.ui.teacher;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Professorship;
import net.sourceforge.fenixedu.injectionCode.AccessControl;
import net.sourceforge.fenixedu.presentationTier.Action.teacher.ManageExecutionCourseDA;
import org.fenixedu.core.ui.StrutsFunctionalityController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;

import static pt.ist.fenixframework.FenixFramework.getDomainObject;

@RestController
@RequestMapping("/teacher/{executionCourseId}/pages")
public class TeacherPagesController extends StrutsFunctionalityController {

    @Autowired
    PagesAdminService service;

    @RequestMapping(method = RequestMethod.GET)
    public TeacherPagesView all(Model model, @PathVariable String executionCourseId) {
        /*ExecutionCourse executionCourse = getDomainObject(executionCourseId);
        Professorship professorship = executionCourse.getProfessorship(AccessControl.getPerson());
        AccessControl.check(person -> professorship != null && professorship.getPermissions().getSections());
        model.addAttribute("executionCourse", executionCourse);
        model.addAttribute("professorship", professorship);
        model.addAttribute("site", executionCourse.getCmsSite());
        return new TeacherPagesView();
        */
        return new TeacherPagesView();
    }


    @Override
    protected Class<?> getFunctionalityType() {
        return ManageExecutionCourseDA.class;
    }

    class TeacherPagesView extends JstlView {

        @Override
        protected void exposeHelpers(HttpServletRequest request) throws Exception {
            setServletContext(request.getServletContext());
            super.exposeHelpers(request);
            request.setAttribute("actual$page", "/teacher/teacherPages.jsp");
        }

        @Override
        public String getUrl() {
            return "/teacher/executionCourse/executionCourseFrame.jsp";
        }

    }
}
