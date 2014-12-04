package pt.ist.fenix.cms.unit;

import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.CMSComponent;
import org.fenixedu.cms.exceptions.ResourceNotFoundException;

public abstract class UnitSiteComponent implements CMSComponent {

    protected Unit unit(Page page) {
        if (page.getSite() instanceof UnitSite) {
            return ((UnitSite) page.getSite()).getUnit();
        }
        throw new ResourceNotFoundException();
    }

}
