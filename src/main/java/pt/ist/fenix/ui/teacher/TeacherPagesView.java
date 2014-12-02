package pt.ist.fenix.ui.teacher;

import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;

public class TeacherPagesView extends JstlView {

    private static TeacherPagesView instance;

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

    public static TeacherPagesView getInstance() {
        if(instance==null) {
            instance = new TeacherPagesView();
        }
        return instance;
    }
}
