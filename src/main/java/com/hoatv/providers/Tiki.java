package com.hoatv.providers;

import com.hoatv.fwk.common.services.GenericHttpClientPool;
import com.hoatv.fwk.common.services.GenericHttpClientPool.ExecutionTemplate;
import com.hoatv.fwk.common.services.HttpClientService;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.fwk.common.ultilities.Pair;
import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.api.ExternalMetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.services.MetricService;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
@ScheduleApplication(application = Tiki.APPLICATION_NAME, period = Tiki.PERIOD_TIME_IN_MILLIS)
@SchedulePoolSettings(application = Tiki.APPLICATION_NAME, threadPoolSettings = @ThreadPoolSettings(name = Tiki.APPLICATION_NAME, numberOfThreads = 30))
@MetricProvider(application = Tiki.APPLICATION_NAME, category = "e-commerce")
public class Tiki implements ExternalMetricProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tiki.class);

    public static final String APPLICATION_NAME = "Tiki";
    protected static final long PERIOD_TIME_IN_MILLIS = 1 * 60 * 1000;

    private static final int MAX_RETRY_TIMES = 10;
    private static final String BASE_URL = "https://tiki.vn";
    private static final String API_URL = "https://api.tiki.vn";

    private static final String PRODUCT_DETAIL = "/api/v2/products/%s?platform=web&include=tag,images,gallery,promotions,badges,stock_item,variants,product_links,discount_tag,ranks,breadcrumbs,top_features,cta_desktop";
    private static final String PRODUCT_DETAIL_2 = "/api/v2/products/%s?platform=web&spid=%s&include=tag,images,gallery,promotions,badges,stock_item,variants,product_links,discount_tag,ranks,breadcrumbs,top_features,cta_desktop";
    private static final String COUPON_DETAIL = "/shopping/v2/promotion/rules?pid=%s&seller_id=1";
    protected static final String COUPON_BY_PERCENT = "by_percent";

    private static final String SAMSUNG_QLED_50_INCHES =  APPLICATION_NAME + " Samsung QLED 50 INCHES";
    private static final String SAMSUNG_4K_50_INCHES_1 =  APPLICATION_NAME + " Samsung 4K 50 INCHES-UA50TU7000";
    private static final String SAMSUNG_4K_50_INCHES_2 =  APPLICATION_NAME + " Samsung 4K 50 INCHES-UA50RU7200";
    private static final String SAMSUNG_QLED_55_INCHES =  APPLICATION_NAME + " Samsung QLED 55 INCHES";
    private static final String SAMSUNG_4K_55_INCHES_1 =  APPLICATION_NAME + " Samsung 4K 55 INCHES-UA55TU8500";
    private static final String SAMSUNG_4K_55_INCHES_2 =  APPLICATION_NAME + " Samsung 4K 55 INCHES-UA55TU8100";

    private final MetricService metricService = new MetricService();
    private final HttpClientService httpClientService = HttpClientService.INSTANCE;
    private final GenericHttpClientPool httpClientPool = new GenericHttpClientPool(30, 2000);
    private final Map<String, Long> additionalProductMap = new ConcurrentHashMap<>();

    private static final BiFunction<Integer, Promotion.Datum, Integer> REDUCE_PRICE_FUNCTION = (price, data) -> {
        String simpleAction = data.getSimple_action();
        if (simpleAction.equals(COUPON_BY_PERCENT)) {
            return price - (price * data.getDiscount_amount() / 100);
        }
        return price - data.getDiscount_amount();
    };

    public void addAdditionalProduct(String productName, Long id) {
        Long productId = additionalProductMap.putIfAbsent(productName, id);
        ObjectUtils.checkThenThrow(Objects::nonNull, productId, "Product "+ productId + " is already monitor");
    }


    @ScheduleTask(name = "COLLECTING_ADDITIONAL_PRODUCTS")
    public void processMetrics() {
        if (additionalProductMap.size() == 0) {
            return;
        }

        BiConsumer<String, Long> consumer = (productName, productId) -> {
            Collection<MetricTag> productPrice = getProductPrice(productId);
            metricService.setMetric(productName, productPrice);
        };
        additionalProductMap.forEach(consumer);
    }

    @ScheduleTask(name = SAMSUNG_QLED_55_INCHES)
    public void getQLED55InchesPriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(50702925L);
        metricService.setMetric(SAMSUNG_QLED_55_INCHES, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_QLED_50_INCHES)
    public void getQLED50InchesPriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(50421565L);
        metricService.setMetric(SAMSUNG_QLED_50_INCHES, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_50_INCHES_1)
    public void get4K50InchesPriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(50421559L);
        metricService.setMetric(SAMSUNG_4K_50_INCHES_1, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_50_INCHES_2)
    public void get4K50Inches2PriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(11656299L);
        metricService.setMetric(SAMSUNG_4K_50_INCHES_2, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_55_INCHES_1)
    public void get4K55Inches1PriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(50702921L);
        metricService.setMetric(SAMSUNG_4K_55_INCHES_1, productPrice);
    }

    @ScheduleTask(name = SAMSUNG_4K_55_INCHES_2)
    public void get4K55Inches2PriceScheduleTask() {
        Collection<MetricTag> productPrice = getProductPrice(50702927L);
        metricService.setMetric(SAMSUNG_4K_55_INCHES_2, productPrice);
    }

    @Override
    @Metric(name = "External")
    public List<ComplexValue> getExternalMetricValues() {
        return additionalProductMap.entrySet().stream().map(p -> {
            ComplexValue metric = metricService.getMetric(p.getKey());
            if (metric == null) {
                return null;
            }

            MetricTag metricTag = metric.getTags().stream().findFirst().orElseThrow();
            metricTag.getAttributes().putIfAbsent("name", p.getKey());
            return metric;
        }).filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    @Metric(name = SAMSUNG_QLED_55_INCHES, unit = "VND")
    public ComplexValue getQLED55InchesPrice() {
        return metricService.getMetric(SAMSUNG_QLED_55_INCHES);
    }

    @Metric(name = SAMSUNG_QLED_50_INCHES, unit = "VND")
    public ComplexValue getQLED50InchesPrice() {
        return metricService.getMetric(SAMSUNG_QLED_50_INCHES);
    }

    @Metric(name = SAMSUNG_4K_50_INCHES_1, unit = "VND")
    public ComplexValue get4K50InchesPrice() {
        return metricService.getMetric(SAMSUNG_4K_50_INCHES_1);
    }

    @Metric(name = SAMSUNG_4K_50_INCHES_2, unit = "VND")
    public ComplexValue get4K50Inches2Price() {
        return metricService.getMetric(SAMSUNG_4K_50_INCHES_2);
    }

    @Metric(name = SAMSUNG_4K_55_INCHES_1, unit = "VND")
    public ComplexValue get4K55Inches1Price() {
        return metricService.getMetric(SAMSUNG_4K_55_INCHES_1);
    }

    @Metric(name = SAMSUNG_4K_55_INCHES_2, unit = "VND")
    public ComplexValue get4K55Inches2Price() {
        return metricService.getMetric(SAMSUNG_4K_55_INCHES_2);
    }

    private Collection<MetricTag> getProductPrice(Long masterId) {
        String productDetailURL = BASE_URL.concat(String.format(PRODUCT_DETAIL, masterId));

        ExecutionTemplate<Collection<MetricTag>> executionTemplate = httpClient -> {
            Product product = httpClientService.sendGETRequest(httpClient, productDetailURL, Product.class,
                    MAX_RETRY_TIMES);
            List<Pair<String, Integer>> sellers = getProductIdList(product);
            Collection<MetricTag> minPrices = getMinPriceFromAllSuppliers(masterId, httpClient, sellers);
            LOGGER.info("Got min price {} for product {}", minPrices, product.getName());
            return minPrices;
        };

        return httpClientPool.executeWithTemplate(executionTemplate);
    }

    private List<Pair<String, Integer>> getProductIdList(Product product) {
        List<Pair<String, Integer>> productIdList = new ArrayList<>();
        int productId = product.getId();

        if (Objects.isNull(product.getCurrent_seller())) {
            LOGGER.warn("Product isn't sell any more: {}", product.getName());
            return productIdList;
        }

        if (Objects.nonNull(product.getTala_request_id())) {
            LOGGER.error("An exception occurred, the detail error message - {}", product.getError().getMessage());
            return productIdList;
        }
        productIdList.add(Pair.of(product.getCurrent_seller().getName(), productId));

        List<Product.OtherSeller> otherSellers = product.getOther_sellers();
        List<Pair<String, Integer>> otherProductIds = otherSellers.stream()
                .map(otherSeller -> Pair.of(otherSeller.getName(), Integer.parseInt(otherSeller.getProduct_id())))
                .collect(Collectors.toList());

        productIdList.addAll(otherProductIds);
        return productIdList;
    }

    private Collection<MetricTag> getMinPriceFromAllSuppliers(Long masterId, HttpClient httpClient, List<Pair<String, Integer>> sellers) {
        List<Long> allListPrice = new ArrayList<>();
        List<MetricTag> allMetricTagList = new ArrayList<>();
        for (Pair<String, Integer> seller : sellers) {
            Integer sellerProductId = seller.getValue();

            String productDetailURL = BASE_URL.concat(String.format(PRODUCT_DETAIL_2, masterId, sellerProductId));
            Product product = httpClientService.sendGETRequest(httpClient, productDetailURL, Product.class,
                    MAX_RETRY_TIMES);

            if (Objects.nonNull(product.getTala_request_id())) {
                LOGGER.warn("An exception occurred, the detail error message - {}", product.getError().getMessage());
            } else if (CollectionUtils.isEmpty(product.getConfigurable_products())) {
                Product.CurrentSeller currentSeller = product.getCurrent_seller();
                if (currentSeller == null) {
                    LOGGER.warn("Product isn't sell any more: {}", product.getName());
                    continue;
                }
                int productId = Integer.parseInt(currentSeller.getProduct_id());
                long minPrice = getMinPrice(httpClient, product.getPrice(), productId);
                allListPrice.add(minPrice);
            } else {
                List<Product.ConfigurableProduct> configurableProducts = product.getConfigurable_products();
                for (Product.ConfigurableProduct configurableProduct : configurableProducts) {
                    long minPrice = getMinPrice(httpClient, configurableProduct.getPrice(), configurableProduct.getId());
                    MetricTag metricTag = new MetricTag(String.valueOf(minPrice));
                    if (StringUtils.isNotEmpty(configurableProduct.getOption1())) {
                        metricTag.getAttributes().put(Product.ConfigurableProduct.Fields.option1, configurableProduct.getOption1());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption2())) {
                        metricTag.getAttributes().put(Product.ConfigurableProduct.Fields.option2, configurableProduct.getOption2());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption3())) {
                        metricTag.getAttributes().put(Product.ConfigurableProduct.Fields.option3, configurableProduct.getOption3());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption4())) {
                        metricTag.getAttributes().put(Product.ConfigurableProduct.Fields.option4, configurableProduct.getOption4());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption5())) {
                        metricTag.getAttributes().put(Product.ConfigurableProduct.Fields.option5, configurableProduct.getOption5());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption4())) {
                        metricTag.getAttributes().put(Product.ConfigurableProduct.Fields.option6, configurableProduct.getOption6());
                    }
                    allMetricTagList.add(metricTag);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(allListPrice)) {
            Optional<Long> min = allListPrice.stream().min(Long::compareTo);
            return List.of(new MetricTag(min.get().toString()));
        }

        Map<String, MetricTag> metricTagMap = allMetricTagList.stream()
                .map(metricTag -> Pair.of(metricTag.getAttributes().toString(), metricTag))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> Long.parseLong(k1.getValue()) > Long.parseLong(k2.getValue()) ? k2 : k1));
        return metricTagMap.values();
    }

    private long getMinPrice(HttpClient httpClient, Integer price, int productId) {
        String disCountURL = API_URL.concat(String.format(COUPON_DETAIL, productId));
        Promotion promotion = httpClientService.sendGETRequest(httpClient, disCountURL, Promotion.class,
                MAX_RETRY_TIMES);
        if (CollectionUtils.isEmpty(promotion.getData())) {
            return price;
        }

        IntSummaryStatistics intSummaryStatistics = promotion.getData().stream()
                .map(data -> REDUCE_PRICE_FUNCTION.apply(price, data))
                .collect(Collectors.summarizingInt(Integer::intValue));
        return intSummaryStatistics.getMin();
    }
}
