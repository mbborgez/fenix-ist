package pt.ist.fenix.cms.homepage;

import net.sourceforge.fenixedu.domain.Person;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.CMSComponent;
import org.fenixedu.cms.exceptions.ResourceNotFoundException;

/**
 * Created by borgez on 02-12-2014.
 */
public abstract class HomepageSiteComponent implements CMSComponent {

    protected Person owner(Page page) {
        return site(page).getOwner();
    }

    protected HomepageSite site(Page page) {
        if (page.getSite() instanceof HomepageSite) {
            return (HomepageSite) page.getSite();
        }
        throw new ResourceNotFoundException();
    }
}
