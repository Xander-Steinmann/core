package com.dotmarketing.quartz.job;

import com.dotcms.contenttype.model.type.ContentType;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.FactoryLocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.quartz.DotStatefulJob;
import com.dotmarketing.util.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import java.io.Serializable;
import java.util.Map;

public class ContentTypeDeleteJob extends DotStatefulJob {
    @Override
    public void run(JobExecutionContext jobContext) throws JobExecutionException {

        final Trigger trigger = jobContext.getTrigger();
        final Map<String, Serializable> map = getExecutionData(trigger, ContentTypeDeleteJob.class);
        final String inode = (String)map.get("inode");
        final String varName = (String)map.get("varName");

        try {
            ContentType contentType = APILocator.getContentTypeAPI(APILocator.systemUser()).find(inode);
            //contentType = APILocator.getContentletAPI().markContentsForDeletion(contentType);
            //Kick-off deletion
            FactoryLocator.getContentTypeFactory().tearDown(contentType);
        } catch (DotDataException | DotSecurityException e) {
            throw new JobExecutionException(
                    String.format("Error removing contentlets from CT with inode [%s] and [%s] .",inode, varName), e);
        }

    }

    public static void triggerContentTypeDeletion(final ContentType type) {

        final Map<String, Serializable> nextExecutionData = Map.of("inode", type.inode(), "varName", type.variable());
        try {
            DotStatefulJob.enqueueTrigger(nextExecutionData, ContentTypeDeleteJob.class);
        } catch (Exception e) {
            Logger.error(ContentTypeDeleteJob.class, String.format("Error scheduling an instance of ContentTypeDeleteJob inode[%s], varName[%s] ",type.inode(), type.variable()), e);
            throw new DotRuntimeException("Error scheduling ContentTypeDeleteJob ", e);
        }
    }
}
