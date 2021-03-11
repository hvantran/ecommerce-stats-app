package com.hoatv.providers;

import com.hoatv.fwk.common.services.HtmlDocumentService;
import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.services.MetricService;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*@ScheduleApplication(application = Lazada.APPLICATION_NAME, period = Lazada.PERIOD_TIME_IN_MILLIS)
@SchedulePoolSettings(application = Lazada.APPLICATION_NAME, threadPoolSettings = @ThreadPoolSettings(name = Lazada.APPLICATION_NAME, numberOfThreads = 30))
@MetricProvider(application = Lazada.APPLICATION_NAME, category = "e-commerce")*/
public class Lazada {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lazada.class);

    protected static final String APPLICATION_NAME = "Lazada";
    protected static final long PERIOD_TIME_IN_MILLIS = 5 * 60 * 1000;

    private static final String PRODUCT_DETAIL_URL = "https://www.lazada.vn/products/x-i%s-x.html";

    private static final String SAMSUNG_QLED_50_INCHES = APPLICATION_NAME + " - " + "Samsung QLED 50 INCHES";
    private static final String SAMSUNG_4K_50_INCHES_1 = APPLICATION_NAME + " - " + "Samsung 4K 50 INCHES-UA50TU7000";
    private static final String SAMSUNG_4K_55_INCHES_1 = APPLICATION_NAME + " - " + "Samsung 4K 55 INCHES-UA55TU8500";
    private static final String SAMSUNG_4K_55_INCHES_2 = APPLICATION_NAME + " - " + "Samsung 4K 55 INCHES-UA55TU8100";

    private static final String NON_DIGIT_PATTERN = "[^0-9]";
    private static final String EMPTY = "";
    private static final String DOT = ".";

    private final MetricService metricService = new MetricService();
    private final HtmlDocumentService htmlDocumentService = HtmlDocumentService.INSTANCE;

    @ScheduleTask(name = SAMSUNG_QLED_50_INCHES)
    public void getQLED50InchesPriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(839294193L);
        metricService.setMetric(SAMSUNG_QLED_50_INCHES, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_50_INCHES_1)
    public void get50Inches1PriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(1168484770L);
        metricService.setMetric(SAMSUNG_4K_50_INCHES_1, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_55_INCHES_1)
    public void getQLED55InchesPriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(914706388l);
        metricService.setMetric(SAMSUNG_4K_55_INCHES_1, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_55_INCHES_2)
    public void getQLED55Inches1PriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(600078887l);
        metricService.setMetric(SAMSUNG_4K_55_INCHES_2, productPrice);
    }

    @Metric(name = SAMSUNG_QLED_50_INCHES, unit = "VND")
    public ComplexValue getQLED50InchesPrice() {
        return metricService.getMetric(SAMSUNG_QLED_50_INCHES);
    }

    @Metric(name = SAMSUNG_4K_50_INCHES_1, unit = "VND")
    public ComplexValue get4K50InchesPrice() {
        return metricService.getMetric(SAMSUNG_4K_50_INCHES_1);
    }

    @Metric(name = SAMSUNG_4K_55_INCHES_1, unit = "VND")
    public ComplexValue get4K55Inches1Price() {
        return metricService.getMetric(SAMSUNG_4K_55_INCHES_1);
    }

    @Metric(name = SAMSUNG_4K_55_INCHES_2, unit = "VND")
    public ComplexValue get4K55Inches2Price() {
        return metricService.getMetric(SAMSUNG_4K_55_INCHES_2);
    }

    private Collection<MetricTag> getProductPrice(long productId) {
        String productDetailURL = String.format(PRODUCT_DETAIL_URL, productId);
        Document document = htmlDocumentService.getRootDocument(productDetailURL);
        Element priceElement = htmlDocumentService.getSelectNode(document,
                "span.pdp-price_type_normal");
        Element titleElement = htmlDocumentService.getSelectNode(document, "title");
        String priceString = priceElement.text();
        List<MetricTag> metricTagList = new ArrayList<>();
        MetricTag priceMetric = new MetricTag(priceString.replace(DOT, EMPTY).replaceAll(NON_DIGIT_PATTERN, EMPTY));
        metricTagList.add(priceMetric);
        LOGGER.info("Got min price {} for product {}", priceMetric, titleElement.text());
        return metricTagList;
    }
}
