package pt.ist.fenix.cms.homepage;

import net.sourceforge.fenixedu.domain.Person;
import org.fenixedu.bennu.core.domain.Bennu;
import pt.ist.fenixframework.Atomic;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

public class HomepageSite extends HomepageSite_Base {
    public HomepageSite(Person person) {
        super();
        setBennu(Bennu.getInstance());
        setName(getLocalizedString("resources.FenixEduLearningResources", "homepage.title", person.getName()));
        setDescription(getLocalizedString("resources.FenixEduLearningResources", "homepage.title", person.getName()));
        setSlug(person.getUser().getUsername());
        setOwner(person);
    }

    @Override
    @Atomic
    public void delete() {
        setOwner(null);
        super.delete();
    }
}
