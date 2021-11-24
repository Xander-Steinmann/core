package com.dotcms.rendering.velocity.viewtools.content.util;

import com.dotcms.api.web.HttpServletRequestThreadLocal;
import com.dotcms.mock.request.FakeHttpRequest;
import com.dotcms.mock.response.BaseResponse;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.util.HostUtil;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.VelocityUtil;
import com.liferay.portal.model.User;
import com.liferay.util.StringPool;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

/**
 * Generic render for items based on a type.
 * It is controlled by convention, basically the component has base default template, it concat the type as a vtl in order to find the resource
 * Also, users can pass their own base path, in case the item render velocity template does not exists will fallbacks to the default one.
 * @author jsanca
 */
public class GenericRenderableItem {

    private final String defaultPath;
    private final String templateName;
    private final Set<String> allowedTypeSet;

    public GenericRenderableItem(final String defaultPath, final String templateName,
                                 final Set<String> allowedTypeSet) {

        this.defaultPath    = defaultPath;
        this.templateName   = templateName;
        this.allowedTypeSet = allowedTypeSet;
    }

    public String toHtml(final Object item, final String type) {

        try {
            final Host host = APILocator.getHostAPI().findDefaultHost(APILocator.systemUser(), false);
            final HttpServletRequest  requestProxy  = new FakeHttpRequest(host.getHostname(), null).request();
            final HttpServletResponse responseProxy = new BaseResponse().response();
            final Context context = VelocityUtil.getInstance().getContext(requestProxy, responseProxy);
            context.put("item", item);

            return VelocityUtil.getInstance().mergeTemplate(allowedTypeSet.contains(type)?
                    this.defaultPath + type + ".vtl": this.defaultPath + templateName, context);
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotRuntimeException(e);
        }
    }

    public String toHtml(final String baseTemplatePath, final Object item, final String type) {

        try {

            if (this.allowedTypeSet.contains(type)) {

                final Optional<User> userOpt = this.resolveUser();
                final User user = userOpt.orElse(APILocator.systemUser());
                final Tuple2<String, Host> hostPathTuple = resolveHost(baseTemplatePath, user);
                final Host host = hostPathTuple._2();
                final String relativePath = hostPathTuple._1();
                final Folder folder = APILocator.getFolderAPI().findFolderByPath(relativePath, host, user, false);
                if (APILocator.getFileAssetAPI().fileNameExists(host, folder, type + ".vtl")) {

                    final HttpServletRequest requestProxy   = new FakeHttpRequest(host.getHostname(), null).request();
                    final HttpServletResponse responseProxy = new BaseResponse().response();
                    final Context context = VelocityUtil.getInstance().getContext(requestProxy, responseProxy);
                    context.put("item", item);

                    return VelocityUtil.getInstance().merge(baseTemplatePath + type + ".vtl", context);
                }
            }

            return this.toHtml(item, type);
        } catch (DotStateException | DotDataException | DotSecurityException e) {

            Logger.error(this, e.getMessage(), e);
            return this.toHtml(item, type);
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotRuntimeException(e);
        }
    }

    private Tuple2<String, Host>  resolveHost(final String baseTemplatePath, final User user) throws DotSecurityException, DotDataException {

        final Tuple2<String, Host> hostPathTuple = Try.of(()-> HostUtil.splitPathHost(baseTemplatePath, user,
                StringPool.FORWARD_SLASH)).get();
        return hostPathTuple._2() == null?
                Tuple.of(hostPathTuple._1(), APILocator.getHostAPI().findDefaultHost(user, false)):
                hostPathTuple;
    }

    private Optional<User> resolveUser() {

        final HttpServletRequest request = HttpServletRequestThreadLocal.INSTANCE.getRequest();
        return null != request?
                Optional.ofNullable(Try.of(()-> WebAPILocator.getUserWebAPI().getUser(request)).getOrNull()):
                Optional.empty();
    }
}
