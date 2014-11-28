package pt.ist.fenix.cms.unit;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import com.google.common.collect.Maps;
import org.joda.time.YearMonthDay;

import static java.util.stream.Collectors.*;

@ComponentType(name = "Unit Organization", description = "Provides the organizational structure for this unit")
public class Organization extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        globalContext.put("unitBean", new UnitFunctionsBean(unit(page)));
    }

    public static class UnitFunctionsBean {
        private final Unit unit;


        public UnitFunctionsBean(Unit unit) {
            this.unit = unit;
        }

        public SortedMap<Function, SortedSet<PersonFunction>> getPersonFunctionsByFunction() {
            return getPersonFunctionsByFunction(getUnit());
        }

        public Stream<UnitFunctionsBean> getSubunitBeans() {
            Predicate<Unit> hasPersons = subunit->!getPersonFunctionsByFunction(subunit).isEmpty();
            return getUnit().getActiveSubUnits(new YearMonthDay()).stream().filter(hasPersons)
                    .sorted(Unit.COMPARATOR_BY_SUBPARTY_AND_NAME_AND_ID).map(UnitFunctionsBean::new);
        }

        private SortedMap<Function, SortedSet<PersonFunction>> getPersonFunctionsByFunction(Unit unit) {
            return unit.getOrderedActiveFunctions().stream().flatMap(function -> function.getActivePersonFunctions().stream())
                    .collect(groupingBy(PersonFunction::getFunction, functionsFactory, toCollection(personFunctionFactory)));
        }

        public Unit getUnit() {
            return unit;
        }

        private static Supplier<TreeMap<Function, SortedSet<PersonFunction>>> functionsFactory =
                () -> Maps.newTreeMap(Function.COMPARATOR_BY_ORDER);

        private static Supplier<SortedSet<PersonFunction>> personFunctionFactory =
                () -> Sets.newTreeSet(PersonFunction.COMPARATOR_BY_PERSON_NAME);
    }
}
