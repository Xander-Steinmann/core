package com.dotmarketing.portlets.contentlet.transform;

import com.dotcms.contenttype.model.field.Field;
import com.dotcms.contenttype.model.field.FieldBuilder;
import com.dotcms.contenttype.model.type.BaseContentType;
import com.dotcms.contenttype.model.type.ContentType;
import com.dotcms.datagen.*;
import com.dotcms.util.IntegrationTestInitService;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.workflows.business.BaseWorkflowIntegrationTest;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@RunWith(DataProviderRunner.class)
public class WidgetTransformerTest extends BaseWorkflowIntegrationTest {

    static Host site;
    static ContentType contentType;
    static Contentlet widgetWithJson;
    static Contentlet widgetWithoutJson;

    final static String VELOCITY_MESSAGE = "Hello World";

    final static String DONT_RENDER_THIS_CODE = "Message from velocity $date";
    final static String YES_RENDER_THIS_CODE = "My value: #set(\"$message\"=\""+ VELOCITY_MESSAGE + "\") $dotJSON.put(\"message\", $message)";





    @BeforeClass
    public static void prepare() throws Exception {



        IntegrationTestInitService.getInstance().init();


        List<com.dotcms.contenttype.model.field.Field> fields = new ArrayList<>();
        fields.add(
                new FieldDataGen()
                        .name("Code")
                        .velocityVarName("code")
                        .required(true)
                        .sortOrder(10)
                        .next()
        );


        ContentType simpleWidgetContentType = new ContentTypeDataGen()
                .baseContentType(BaseContentType.WIDGET)
                .name("SimpleWidget" + System.currentTimeMillis())
                .velocityVarName("SimpleWidget" + System.currentTimeMillis())
                .fields(fields)
                .nextPersisted();



        Field codeField = simpleWidgetContentType.fieldMap().get("widgetCode");
        APILocator.getContentTypeFieldAPI().save(FieldBuilder.builder(codeField).values("$code").sortOrder(100).build(), APILocator.systemUser());




        site = new SiteDataGen().nextPersisted();

        Contentlet con = new Contentlet();
        con.setContentType(simpleWidgetContentType);
        con.setStringProperty("code", DONT_RENDER_THIS_CODE);
        con.setStringProperty("widgetTitle", "NoRender");
        widgetWithoutJson = ContentletDataGen.checkin(con);


        con = new Contentlet();
        con.setContentType(simpleWidgetContentType);
        con.setStringProperty("code", YES_RENDER_THIS_CODE);
        con.setStringProperty("widgetTitle", "NoRender");
        widgetWithJson = ContentletDataGen.checkin(con);



    }

    @Test
    public void test_widget_returns_NO_json() throws DotDataException, DotSecurityException {


        Map<String, Object> map = new DotTransformerBuilder().defaultOptions().content(widgetWithoutJson).build().toMaps().get(0);
        Map<String,Object> widgetCode = (Map<String, Object>)map.get("widgetCodeJSON");
        assertTrue(widgetCode.isEmpty());



    }

    @Test
    public void test_widget_returns_WITH_json() throws DotDataException, DotSecurityException {


        Map<String, Object> map = new DotTransformerBuilder().defaultOptions().content(widgetWithoutJson).build().toMaps().get(0);
        Map<String,Object> widgetCode = (Map<String, Object>)map.get("widgetCodeJSON");
        assertFalse(widgetCode.isEmpty());


        assertEquals(VELOCITY_MESSAGE,widgetCode.get("message"));


    }

}
