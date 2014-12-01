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
import org.fenixedu.cms.domain.component.DynamicComponent;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.*;

@ComponentType(name = "Presentation Component", description = "Provides homepage owner's presentation data.")
public class PresentationComponent extends PresentationComponent_Base {

    @DynamicComponent
    public PresentationComponent() {
    }

    @Override
    public void handle(Page page, TemplateContext local, TemplateContext global) {
        HomepageSite site = (HomepageSite) page.getSite();
        Person owner = site.getOwner();

        global.put("ownerName", owner.getNickname());

        if (getShowPhoto()) {
            global.put("ownerId", owner.getExternalId());
        }

        Employee employee = owner.getEmployee();
        if (employee != null) {
            Contract contract = owner.getEmployee().getCurrentWorkingContract();
            if (contract != null) {
                if (getShowUnit()) {
                    global.put("workingUnit", contract.getWorkingUnit());
                }
                if (getShowCategory() && owner.getTeacher() != null) {
                    global.put("teacherCategory", owner.getTeacher().getCategory().getName().getContent());
                }
            }
        }

        if (getShowResearchUnitHomepage()) {
            List<ResearchUnit> researchUnits = owner.getWorkingResearchUnits();
            if (researchUnits.isEmpty()) {
                if (owner.getTeacher() != null && owner.getEmployee().getCurrentWorkingContract() != null) {
                    global.put("researchUnitName", getResearchUnitName());
                    global.put("researchUnitHomepage", getResearchUnitHomepage());
                }
            } else {
                global.put("workingResearchUnits", owner.getWorkingResearchUnits());
            }
        }

        if (getShowActiveStudentCurricularPlans()) {
            global.put("activeCurricularPlans", owner.getActiveStudentCurricularPlansSortedByDegreeTypeAndDegreeName());
        }

        if (getShowCurrentAttendingExecutionCourses()) {
            SortedSet<Attends> attendedCoursesByName = new TreeSet<Attends>(Attends.ATTENDS_COMPARATOR_BY_EXECUTION_COURSE_NAME);
            attendedCoursesByName.addAll(owner.getCurrentAttends());
            global.put("attendingCourses", attendedCoursesByName);
        }

        if (getShowAlumniDegrees()) {
            global.put("completedCurricularPlans", owner.getCompletedStudentCurricularPlansSortedByDegreeTypeAndDegreeName());
        }

        if (getShowEmail()) {
            List<? extends PartyContact> emails = owner.getEmailAddresses();
            global.put("emails", getSortedFilteredContacts(emails));
        }

        List<? extends PartyContact> phones = owner.getPhones();
        if (getShowPersonalTelephone()) {
            global.put("personalPhones", getSortedFilteredContacts(phones, PartyContactType.PERSONAL));
        }

        if (getShowWorkTelephone()) {
            global.put("workPhones", getSortedFilteredContacts(phones, PartyContactType.WORK));
        }

        if (getShowMobileTelephone()) {
            List<? extends PartyContact> mobilePhones = owner.getMobilePhones();
            global.put("mobilePhones", getSortedFilteredContacts(mobilePhones));
        }

        if (getShowAlternativeHomepage()) {
            List<? extends PartyContact> websites = owner.getWebAddresses();
            global.put("websites", getSortedFilteredContacts(websites));
        }

        if (getShowCurrentExecutionCourses() && owner.getTeacher() != null
                && owner.getEmployee().getCurrentWorkingContract() != null) {
            global.put("teachingCourses", owner.getTeacher().getCurrentExecutionCourses());
        }

        //TODO Unit, ResearchUnit, PartyContact, StudentCurricularPlan, Attends and ExecutionCourse wrappers

    }

    private boolean isVisible(PartyContact contact) {
        boolean publicSpace = true; //because this is a homepage. When this logic is exported to a more proper place remember to pass this as an argument.
        if (!Authenticate.isLogged() && publicSpace && contact.getVisibleToPublic().booleanValue()) {
            return true;
        }
        if (Authenticate.isLogged()) {
            User user = Authenticate.getUser();
            Person reader = user.getPerson();
            if (reader.hasRole(RoleType.CONTACT_ADMIN).booleanValue() || reader.hasRole(RoleType.MANAGER).booleanValue()
                    || reader.hasRole(RoleType.DIRECTIVE_COUNCIL).booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.EMPLOYEE).booleanValue() && contact.getVisibleToEmployees().booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.TEACHER).booleanValue() && contact.getVisibleToTeachers().booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.STUDENT).booleanValue() && contact.getVisibleToStudents().booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.ALUMNI).booleanValue() && contact.getVisibleToAlumni().booleanValue()) {
                return true;
            }
            if (contact.getVisibleToPublic()) {
                return true;
            }
        }
        return false;
    }

    protected List<PartyContact> getSortedFilteredContacts(Collection<? extends PartyContact> unfiltered,
            PartyContactType... types) {
        List<PartyContactType> typeList;
        if (types.length == 0) {
            typeList = Arrays.asList(PartyContactType.values());
        } else {
            typeList = Arrays.asList(types);
        }

        List<PartyContact> contacts = new ArrayList<PartyContact>();
        for (PartyContact contact : unfiltered) {
            if (isVisible(contact) && typeList.contains(contact.getType())) {
                contacts.add(contact);
            }
        }

        Collections.sort(contacts, new Comparator<PartyContact>() {
            @Override
            public int compare(PartyContact contact1, PartyContact contact2) {
                if (contact1.getType().ordinal() > contact2.getType().ordinal()) {
                    return -1;
                } else if (contact1.getType().ordinal() < contact2.getType().ordinal()) {
                    return 1;
                } else if (contact1.getDefaultContact().booleanValue()) {
                    return -1;
                } else if (contact2.getDefaultContact().booleanValue()) {
                    return 1;
                } else {
                    return contact1.getPresentationValue().compareTo(contact2.getPresentationValue());
                }
            }
        });
        return contacts;
    }

}
