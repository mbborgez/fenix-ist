package pt.ist.fenix.ui.spring;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.exceptions.BennuCoreDomainException;
import org.fenixedu.bennu.core.groups.AnyoneGroup;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.io.domain.GroupBasedFile;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.cms.domain.*;
import org.fenixedu.cms.exceptions.CmsDomainException;
import org.fenixedu.commons.i18n.LocalizedString;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import pt.ist.fenix.domain.unit.UnitSite;
import pt.ist.fenixframework.FenixFramework;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.fenixedu.bennu.io.servlets.FileDownloadServlet.getDownloadUrl;

/**
 * Created by borgez on 18-03-2015.
 */
@SpringApplication(group = "logged", path = "unit-sites", title = "unit.site.management.title")
@SpringFunctionality(accessGroup = "logged", app = UnitSiteManagementController.class, title = "unit.site.management.title")
@RequestMapping("/unit/sites")
public class UnitSiteManagementController {

    @RequestMapping
    public String allSites(Model model) {
        model.addAttribute("unitSites", unitSitesForUser(Authenticate.getUser()));
        return "fenix-learning/unitSites";
    }

    private Collection<Site> unitSitesForUser(User user) {
        Set<Site> allSites = Bennu.getInstance().getSitesSet();
        Predicate<Site> isAdminMember = site -> site.getCanAdminGroup().isMember(user);
        Predicate<Site> isUnitSite = site -> site instanceof UnitSite;
        return allSites.stream().filter(isUnitSite.and(isAdminMember)).collect(toList());
    }

    @RequestMapping(value = "/{unitSiteSlug}")
    public String manageSite(Model model, @PathVariable String unitSiteSlug) {
        Site unitSite = site(unitSiteSlug);
        model.addAttribute("banners", getBanners(unitSite));
        model.addAttribute("unitSite", unitSite);
        return "fenix-learning/unitSiteManagement";
    }

    private List<BannerBean> getBanners(Site unitSite) {
        Category category = unitSite.categoryForSlug("banner");
        if (category != null) {
            return category.getPostsSet().stream()
                    .filter(post -> post.getMetadata() != null).map(BannerBean::new).collect(toList());
        }
        return ImmutableList.of();
    }

    @RequestMapping(value = "/{unitSiteSlug}/layout", method = RequestMethod.POST)
    public RedirectView editLayout(@PathVariable String unitSiteSlug, @RequestParam String template) {
        Site unitSite = site(unitSiteSlug);
        CMSTemplate cmsTemplate = unitSite.getTheme().templateForType(template);
        if (cmsTemplate != null && !cmsTemplate.equals(unitSite.getInitialPage().getTemplate())) {
            FenixFramework.atomic(() -> {
                unitSite.getInitialPage().setTemplate(cmsTemplate);
            });
        }
        return new RedirectView("/unit/sites", true);
    }

    @RequestMapping(value = "/{unitSiteSlug}/create", method = RequestMethod.POST)
    public RedirectView createBanner(@PathVariable String unitSiteSlug, BannerBean banner) {
        Site unitSite = site(unitSiteSlug);
        FenixFramework.atomic(() -> {
            banner.setPost(new Post(unitSite));
            banner.save();
        });
        return defaultRedirect(unitSite);
    }

    @RequestMapping(value = "/{unitSiteSlug}/{postSlug}/update", method = RequestMethod.POST)
    public RedirectView updateBanner(@PathVariable String unitSiteSlug, @PathVariable String postSlug, BannerBean banner) {
        Site unitSite = site(unitSiteSlug);
        FenixFramework.atomic(() -> {
            banner.setPost(unitSite.postForSlug(postSlug));
            banner.save();
        });
        return defaultRedirect(unitSite);
    }

    @RequestMapping(value = "/{unitSiteSlug}/{postSlug}/delete", method = RequestMethod.POST)
    public RedirectView deleteBanner(@PathVariable String unitSiteSlug, @PathVariable String postSlug) {
        Site unitSite = site(unitSiteSlug);
        FenixFramework.atomic(() -> {
            Post post = unitSite.postForSlug(postSlug);
            if (FenixFramework.isDomainObjectValid(post)) {
                post.delete();
            }
        });
        return defaultRedirect(unitSite);
    }

    private RedirectView defaultRedirect(Site unitSite) {
        return new RedirectView(String.format("/unit/sites/%s", unitSite.getSlug()), true);
    }

    private Site site(String unitSiteSlug) {
        Site site = Site.fromSlug(unitSiteSlug);
        if (!FenixFramework.isDomainObjectValid(site)) {
            throw BennuCoreDomainException.resourceNotFound(unitSiteSlug);
        }
        if (site instanceof UnitSite) {
            if (!site.getCanAdminGroup().isMember(Authenticate.getUser())) {
                throw CmsDomainException.forbiden();
            }
        }
        return site;
    }


    public static class BannerBean {
        private LocalizedString name;
        private Boolean showIntroduction;
        private Boolean showAnnouncements;
        private Boolean showEvents;
        private Boolean showBanner;
        private String color;
        private MultipartFile mainImage;
        private MultipartFile backgroundImage;
        private String mainImageUrl;
        private String backgroundImageUrl;
        private Post post;

        public BannerBean() {
        }

        public BannerBean(Post post) {
            this.post = post;
            this.name = post.getName();
            this.showIntroduction = post.getMetadata().getAsBoolean("showIntroduction").orElse(true);
            this.showAnnouncements = post.getMetadata().getAsBoolean("showAnnouncements").orElse(true);
            this.showEvents = post.getMetadata().getAsBoolean("showEvents").orElse(true);
            this.showBanner = post.getMetadata().getAsBoolean("showBanner").orElse(true);
            this.color = post.getMetadata().getAsString("color").orElse("white");
            this.mainImageUrl = post.getMetadata().getAsString("mainImage").orElse(null);
            this.backgroundImageUrl = post.getMetadata().getAsString("backgroundImage").orElse(null);
        }

        public void save() {
            PostMetadata postMetadata = ofNullable(post.getMetadata()).orElseGet(PostMetadata::new);
            Category bannerCategory = getOrCreateBannerCategory(post.getSite());

            if (!post.getCategoriesSet().contains(bannerCategory)) {
                post.addCategories(bannerCategory);
            }

            if (name != null) {
                post.setName(name);
            }

            postMetadata = postMetadata.with("showIntroduction", ofNullable(showIntroduction).orElse(false));
            postMetadata = postMetadata.with("showAnnouncements", ofNullable(showAnnouncements).orElse(false));
            postMetadata = postMetadata.with("showEvents", ofNullable(showEvents).orElse(false));
            postMetadata = postMetadata.with("showBanner", ofNullable(showBanner).orElse(false));
            postMetadata = postMetadata.with("color", ofNullable(color).orElse("#ffffff"));
            postMetadata = uploadImage(post, postMetadata, "mainImage", mainImage);

            post.setMetadata(postMetadata);
        }

        private PostMetadata uploadImage(Post post, PostMetadata postMetadata, String name, MultipartFile multipartFile) {
            if (!Strings.isNullOrEmpty(name) && multipartFile != null && !multipartFile.isEmpty()) {
                try {
                    GroupBasedFile file = new GroupBasedFile(multipartFile.getOriginalFilename(), multipartFile.getOriginalFilename(), multipartFile.getBytes(), AnyoneGroup.get());
                    post.getPostFiles().putFile(file);
                    postMetadata = postMetadata.with(name, getDownloadUrl(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return postMetadata;
        }

        private Category getOrCreateBannerCategory(Site site) {
            return site.getOrCreateCategoryForSlug("banner", new LocalizedString(Locale.getDefault(), "Banner"));
        }

        /*
        * getters and setters
        * */

        public Post getPost() {
            return this.post;
        }

        public void setPost(Post post) {
            this.post = post;
        }


        public LocalizedString getName() {
            return name;
        }

        public void setName(LocalizedString name) {
            this.name = name;
        }

        public Boolean getShowIntroduction() {
            return showIntroduction;
        }

        public void setShowIntroduction(Boolean showIntroduction) {
            this.showIntroduction = showIntroduction;
        }

        public Boolean getShowAnnouncements() {
            return showAnnouncements;
        }

        public void setShowAnnouncements(Boolean showAnnouncements) {
            this.showAnnouncements = showAnnouncements;
        }

        public Boolean getShowEvents() {
            return showEvents;
        }

        public void setShowEvents(Boolean showEvents) {
            this.showEvents = showEvents;
        }

        public Boolean getShowBanner() {
            return showBanner;
        }

        public void setShowBanner(Boolean showBanner) {
            this.showBanner = showBanner;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public MultipartFile getMainImage() {
            return mainImage;
        }

        public void setMainImage(MultipartFile mainImage) {
            this.mainImage = mainImage;
        }


        public String getMainImageUrl() {
            return mainImageUrl;
        }

        public void setMainImageUrl(String mainImageUrl) {
            this.mainImageUrl = mainImageUrl;
        }

    }
}
