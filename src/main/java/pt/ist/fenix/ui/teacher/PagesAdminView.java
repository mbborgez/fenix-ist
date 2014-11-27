package pt.ist.fenix.ui.teacher;

import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;

public class PagesAdminView extends JstlView {

    private static PagesAdminView instance;

    @Override
    protected void exposeHelpers(HttpServletRequest request) throws Exception {
        setServletContext(request.getServletContext());
        super.exposeHelpers(request);
        request.setAttribute("teacher$actual$page", "/teacher/pages.jsp");
    }

    @Override
    public String getUrl() {
        return "/teacher/executionCourse/executionCourseFrame.jsp";
    }

    public static PagesAdminView getInstance() {
        if(instance==null) {
            instance = new PagesAdminView();
        }
        return instance;
    }
}
