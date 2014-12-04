package pt.ist.fenix.cms.unit;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.Employee;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.person.RoleType;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

@ComponentType(name = "departmentEmployees", description = "Employees information for a Department")
public class EmployeesComponent extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        SortedMap<Unit, TreeSet<Employee>> employeesMap = nonTeacherEmployeesByWorkingPlace(unit(page));
        globalContext.put("hasEmployeesNoArea", nonTeacherEmployeesWithoutWorkingPlace(unit(page)).findAny().isPresent());
        globalContext.put("employeesNoArea", nonTeacherEmployeesWithoutWorkingPlace(unit(page)));
        globalContext.put("employeesByArea", employeesMap);
    }

    private SortedMap<Unit, TreeSet<Employee>> nonTeacherEmployeesByWorkingPlace(Unit unit) {
        return nonTeacherEmployeesWithWorkingPlace(unit)
                .collect(groupingBy(Employee::getCurrentWorkingPlace, mapFactory, toCollection(sortedEmployeesFactory)));
    }

    private Stream<Employee> nonTeacherEmployeesWithoutWorkingPlace(Unit unit) {
        return nonTeacherEmployees(unit).filter(employee->employee.getCurrentWorkingPlace()==null).sorted(comparator);
    }

    private Stream<Employee> nonTeacherEmployeesWithWorkingPlace(Unit unit) {
        return nonTeacherEmployees(unit).filter(employee -> employee.getCurrentWorkingPlace() != null);
    }

    private Stream<Employee> nonTeacherEmployees(Unit unit) {
        return unit.getAllCurrentNonTeacherEmployees().stream().filter(isNotTeacher).sorted(comparator);
    }

    private static Comparator<Employee> comparator = (Employee e1, Employee e2) -> e1.getPerson().compareTo(e2.getPerson());

    private static Supplier<TreeMap<Unit, TreeSet<Employee>>> mapFactory = ()->Maps.newTreeMap(Unit.COMPARATOR_BY_NAME_AND_ID);

    private static Supplier<TreeSet<Employee>> sortedEmployeesFactory = () -> Sets.newTreeSet(comparator);

    private static Predicate<Employee> isNotTeacher = employee -> !employee.getPerson().hasRole(RoleType.TEACHER);
}