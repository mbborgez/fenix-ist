package pt.ist.fenix.cms.homepage;

import net.sourceforge.fenixedu.domain.Attends;
import net.sourceforge.fenixedu.domain.Employee;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.contacts.PartyContact;
import net.sourceforge.fenixedu.domain.contacts.PartyContactType;
import net.sourceforge.fenixedu.domain.organizationalStructure.Contract;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import net.sourceforge.fenixedu.domain.person.RoleType;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.fenixedu.domain.person.RoleType.*;
import static net.sourceforge.fenixedu.domain.person.RoleType.CONTACT_ADMIN;
import static net.sourceforge.fenixedu.domain.person.RoleType.EMPLOYEE;
import static org.fenixedu.bennu.core.security.Authenticate.getUser;

@ComponentType(name = "Presentation Component", description = "Provides homepage owner's presentation data.")
public class PresentationComponent extends HomepageSiteComponent {

    @Override
    public void handle(Page page, TemplateContext local, TemplateContext global) {
        HomepageSite site = site(page);
        Person owner = owner(page);

        global.put("ownerName", owner.getNickname());

        if (site.getShowPhoto()) {
            global.put("ownerId", owner.getExternalId());
        }

        Employee employee = owner.getEmployee();
        if (employee != null) {
            Contract contract = owner.getEmployee().getCurrentWorkingContract();
            if (contract != null) {
                if (site.getShowUnit()) {
                    global.put("workingUnit", contract.getWorkingUnit());
                }
                if (site.getShowCategory() && owner.getTeacher() != null) {
                    global.put("teacherCategory", owner.getTeacher().getCategory().getName().getContent());
                }
            }
        }

        if (site.getShowResearchUnitHomepage()) {
            List<ResearchUnit> researchUnits = owner.getWorkingResearchUnits();
            if (researchUnits.isEmpty()) {
                if (owner.getTeacher() != null && owner.getEmployee().getCurrentWorkingContract() != null) {
                    global.put("researchUnitName", site.getResearchUnitName());
                    global.put("researchUnitHomepage", site.getResearchUnitHomepage());
                }
            } else {
                global.put("workingResearchUnits", owner.getWorkingResearchUnits());
            }
        }

        if (site.getShowActiveStudentCurricularPlans()) {
            global.put("activeCurricularPlans", owner.getActiveStudentCurricularPlansSortedByDegreeTypeAndDegreeName());
        }

        if (site.getShowCurrentAttendingExecutionCourses()) {
            global.put("attendingCourses", owner.getCurrentAttends().stream()
                    .sorted(Attends.ATTENDS_COMPARATOR_BY_EXECUTION_COURSE_NAME).collect(Collectors.toList()));
        }

        if (site.getShowAlumniDegrees()) {
            global.put("completedCurricularPlans", owner.getCompletedStudentCurricularPlansSortedByDegreeTypeAndDegreeName());
        }

        if (site.getShowEmail()) {
            global.put("emails", getSortedFilteredContacts(owner.getEmailAddresses()));
        }

        if (site.getShowPersonalTelephone()) {
            global.put("personalPhones", getSortedFilteredContacts(owner.getPhones(), PartyContactType.PERSONAL));
        }

        if (site.getShowWorkTelephone()) {
            global.put("workPhones", getSortedFilteredContacts(owner.getPhones(), PartyContactType.WORK));
        }

        if (site.getShowMobileTelephone()) {
            global.put("mobilePhones", getSortedFilteredContacts(owner.getMobilePhones()));
        }

        if (site.getShowAlternativeHomepage()) {
            global.put("websites", getSortedFilteredContacts(owner.getWebAddresses()));
        }

        if (site.getShowCurrentExecutionCourses() && owner.getTeacher() != null
                && owner.getEmployee().getCurrentWorkingContract() != null) {
            global.put("teachingCourses", owner.getTeacher().getCurrentExecutionCourses());
        }

        //TODO Unit, ResearchUnit, PartyContact, StudentCurricularPlan, Attends and ExecutionCourse wrappers

    }

    private boolean isVisible(PartyContact contact) {
        if (!Authenticate.isLogged() || getUser().getPerson() == null) {
            return contact.getVisibleToPublic();
        } else {
            Person reader = getUser().getPerson();
            return (reader.hasRole(CONTACT_ADMIN) || reader.hasRole(MANAGER) || reader.hasRole(DIRECTIVE_COUNCIL))
                    || (reader.hasRole(EMPLOYEE) && contact.getVisibleToEmployees())
                    || (reader.hasRole(TEACHER) && contact.getVisibleToTeachers())
                    || (reader.hasRole(STUDENT) && contact.getVisibleToStudents())
                    || (reader.hasRole(ALUMNI) && contact.getVisibleToAlumni())
                    || (contact.getVisibleToPublic());
        }
    }

    private List<PartyContact> getSortedFilteredContacts(Collection<? extends PartyContact> unfiltered, PartyContactType... types) {
        List<PartyContactType> typeList = types.length == 0 ? asList(PartyContactType.values()) : asList(types);

        return unfiltered.stream().filter(this::isVisible)
                .filter(contact -> typeList.contains(contact.getType()))
                .sorted(PARTY_CONTACT_COMPARATOR)
                .map(PartyContact.class::cast)
                .collect(Collectors.toList());
    }

    private static Comparator<PartyContact> PARTY_CONTACT_COMPARATOR = (contact1, contact2) -> {
        if (contact1.getType().ordinal() > contact2.getType().ordinal()) {
            return -1;
        } else if (contact1.getType().ordinal() < contact2.getType().ordinal()) {
            return 1;
        } else if (contact1.getDefaultContact()) {
            return -1;
        } else if (contact2.getDefaultContact()) {
            return 1;
        } else {
            return contact1.getPresentationValue().compareTo(contact2.getPresentationValue());
        }
    };

}
