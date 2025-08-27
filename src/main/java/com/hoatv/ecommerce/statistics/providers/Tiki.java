package com.hoatv.ecommerce.statistics.providers;

import com.hoatv.fwk.common.services.BiCheckedFunction;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.GenericHttpClientPool;
import com.hoatv.fwk.common.services.GenericHttpClientPool.ExecutionTemplate;
import com.hoatv.fwk.common.services.HttpClientService;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams;
import com.hoatv.fwk.common.services.HttpClientService.RequestParams.RequestParamsBuilder;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.fwk.common.ultilities.Pair;
import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.services.MetricService;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import com.hoatv.task.mgmt.entities.TaskEntry;
import com.hoatv.task.mgmt.services.ScheduleTaskMgmtService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hoatv.fwk.common.constants.MetricProviders.OTHER_APPLICATION;

@ScheduleApplication(application = Tiki.APPLICATION_NAME, period = Tiki.PERIOD_TIME_IN_MILLIS)
@SchedulePoolSettings(application = Tiki.APPLICATION_NAME, threadPoolSettings = @ThreadPoolSettings(name = Tiki.APPLICATION_NAME, numberOfThreads = Tiki.MAXIMUM_NUMBER_OF_PRODUCTS))
@MetricProvider(application = OTHER_APPLICATION, category = "e-commerce")
public class Tiki {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tiki.class);

    public static final String APPLICATION_NAME = "Tiki";
    protected static final long PERIOD_TIME_IN_MILLIS = 60000;

    private static final int MAX_RETRY_TIMES = 50;
    private static final String BASE_URL = "https://tiki.vn";
    private static final String API_URL = "https://api.tiki.vn";

    private static final String PRODUCT_DETAIL = "/api/v2/products/%s?platform=web&include=tag,images,gallery,promotions,badges,stock_item,variants,product_links,discount_tag,ranks,breadcrumbs,top_features,cta_desktop";
    private static final String PRODUCT_DETAIL_2 = "/api/v2/products/%s?platform=web&spid=%s&include=tag,images,gallery,promotions,badges,stock_item,variants,product_links,discount_tag,ranks,breadcrumbs,top_features,cta_desktop";
    private static final String COUPON_DETAIL = "/shopping/v2/promotion/rules?pid=%s&seller_id=1";
    protected static final String COUPON_BY_PERCENT = "by_percent";
    protected static final int MAXIMUM_NUMBER_OF_PRODUCTS = 30;

    private final MetricService metricService = new MetricService();
    private final HttpClientService httpClientService = HttpClientService.INSTANCE;
    private final GenericHttpClientPool httpClientPool = new GenericHttpClientPool(30, 2000);
    private final Map<Long, EMonitorVO> additionalProductMap = new ConcurrentHashMap<>();
    public ScheduleTaskMgmtService scheduleTaskMgmtService;

    private BiFunction<Integer, Promotion.Datum, Integer> getDiscountPrice(String category) {
        return (price, data) -> {
            if (!category.equals(data.getCoupon_type()) || CollectionUtils.isNotEmpty(data.getTags())) {
                return 0;
            }

            String simpleAction = data.getSimple_action();
            if (simpleAction.equals(COUPON_BY_PERCENT)) {
                return price * data.getDiscount_amount() / 100;
            }
            return data.getDiscount_amount();
        };
    }

    public void addAdditionalProduct(EMonitorVO product) {
        String taskName = "product-price" + product.getProductName();
        BiCheckedFunction<Object, Method, TaskEntry> taskEntryFunc = TaskEntry.fromMethodWithParams(taskName, -1, -1, product.getMasterId(), product);
        CheckedSupplier<Method> collectPrice = () -> Tiki.class.getMethod("collectPrice", Long.class, EMonitorVO.class);
        TaskEntry taskEntry = taskEntryFunc.apply(this, collectPrice.get());
        scheduleTaskMgmtService.scheduleFixedRateTask(taskEntry, 5, TimeUnit.SECONDS);
        EMonitorVO productId = additionalProductMap.putIfAbsent(product.getMasterId(), product);
        ObjectUtils.checkThenThrow(Objects::nonNull, productId, "Product "+ productId + " is already monitor");
    }

    public void collectPrice(Long productId, EMonitorVO productMonitor) {
        Collection<MetricTag> productPrice = getProductPrice(productId);
        if (CollectionUtils.isEmpty(productPrice)) {
            return;
        }
        if (StringUtils.isNotEmpty(productMonitor.getSubCategory())) {
            productPrice.forEach(metricTag -> metricTag.add("sub_category", productMonitor.getSubCategory()));
        }
        metricService.setMetric(productMonitor.getProductName(), productPrice);
    }

    @Metric(name = "Product Collection")
    public List<ComplexValue> getExternalMetricValues() {
        return additionalProductMap.values().stream().map(eMonitorVO -> {
            ComplexValue metric = metricService.getMetric(eMonitorVO.getProductName());
            if (metric == null) {
                return null;
            }

            metric.getTags().forEach(tag -> tag.add("name", eMonitorVO.getProductName()));
            return metric;
        }).filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    private Collection<MetricTag> getProductPrice(Long masterId) {
        String productDetailURL = BASE_URL.concat(String.format(PRODUCT_DETAIL, masterId));

        ExecutionTemplate<Collection<MetricTag>> executionTemplate = httpClient -> {

            RequestParamsBuilder requestParamsBuilder = RequestParams.builder(productDetailURL, httpClient)
                    .method(HttpClientService.HttpMethod.GET)
                    .retryTimes(MAX_RETRY_TIMES);

            Product product = httpClientService.sendHTTPRequest()
                    .andThen(response -> HttpClientService.asObject(response, Product.class))
                    .apply(requestParamsBuilder.build());
            if (Objects.isNull(product) || Objects.isNull(product.getCurrent_seller())) {
                LOGGER.warn("Product isn't sell any more: {}", masterId);
                return Collections.emptyList();
            }
            List<Pair<String, Integer>> sellers = getProductIdList(product);
            Collection<MetricTag> minPrices = getMinPriceFromAllSuppliers(masterId, httpClient, sellers);
            LOGGER.info("Got min price {} for product {}", minPrices, product.getName());
            return minPrices;
        };

        return httpClientPool.executeWithTemplate(executionTemplate);
    }

    private List<Pair<String, Integer>> getProductIdList(Product product) {
        List<Pair<String, Integer>> productIdList = new ArrayList<>();

        if (Objects.nonNull(product.getTala_request_id())) {
            LOGGER.error("An exception occurred, the detail error message - {}", product.getError().getMessage());
            return productIdList;
        }
        int currentProductId = Integer.parseInt(product.getCurrent_seller().getProduct_id());
        productIdList.add(Pair.of(product.getCurrent_seller().getName(), currentProductId));

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

            RequestParamsBuilder requestParamsBuilder = RequestParams.builder(productDetailURL, httpClient)
                    .method(HttpClientService.HttpMethod.GET)
                    .retryTimes(MAX_RETRY_TIMES);
            Product product = httpClientService.sendHTTPRequest()
                    .andThen(response -> HttpClientService.asObject(response, Product.class))
                    .apply(requestParamsBuilder.build());
            if (product == null) {
                LOGGER.warn("Product isn't sell any more: {}", masterId);
                continue;
            }

            if (Objects.nonNull(product.getTala_request_id())) {
                LOGGER.warn("An exception occurred, the detail error message - {}", product.getError().getMessage());
            } else if (CollectionUtils.isEmpty(product.getConfigurable_products())) {
                Product.CurrentSeller currentSeller = product.getCurrent_seller();
                int productId = Integer.parseInt(currentSeller.getProduct_id());
                long minPrice = getMinPrice(httpClient, product.getPrice(), productId);
                allListPrice.add(minPrice);
            } else {
                List<Product.ConfigurableProduct> configurableProducts = product.getConfigurable_products();
                for (Product.ConfigurableProduct configurableProduct : configurableProducts) {
                    long minPrice = getMinPrice(httpClient, configurableProduct.getPrice(), configurableProduct.getId());
                    MetricTag metricTag = new MetricTag(String.valueOf(minPrice));
                    if (StringUtils.isNotEmpty(configurableProduct.getOption1())) {
                        metricTag.add(Product.ConfigurableProduct.Fields.option1, configurableProduct.getOption1());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption2())) {
                        metricTag.add(Product.ConfigurableProduct.Fields.option2, configurableProduct.getOption2());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption3())) {
                        metricTag.add(Product.ConfigurableProduct.Fields.option3, configurableProduct.getOption3());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption4())) {
                        metricTag.add(Product.ConfigurableProduct.Fields.option4, configurableProduct.getOption4());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption5())) {
                        metricTag.add(Product.ConfigurableProduct.Fields.option5, configurableProduct.getOption5());
                    }
                    if (StringUtils.isNotEmpty(configurableProduct.getOption4())) {
                        metricTag.add(Product.ConfigurableProduct.Fields.option6, configurableProduct.getOption6());
                    }
                    allMetricTagList.add(metricTag);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(allListPrice)) {
            Optional<Long> min = allListPrice.stream().min(Long::compareTo);
            return List.of(new MetricTag(min.orElse(0L).toString()));
        }

        Map<String, MetricTag> metricTagMap = allMetricTagList.stream()
                .map(metricTag -> Pair.of(metricTag.getAttributes().toString(), metricTag))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (k1, k2) -> Long.parseLong(k1.getValue()) > Long.parseLong(k2.getValue()) ? k2 : k1));
        return metricTagMap.values();
    }

    private long getMinPrice(HttpClient httpClient, Integer price, int productId) {
        String disCountURL = API_URL.concat(String.format(COUPON_DETAIL, productId));

        RequestParamsBuilder requestParamsBuilder = RequestParams.builder(disCountURL, httpClient)
                .method(HttpClientService.HttpMethod.GET)
                .retryTimes(MAX_RETRY_TIMES);
        Promotion promotion = httpClientService.sendHTTPRequest()
                .andThen(response -> HttpClientService.asObject(response, Promotion.class))
                .apply(requestParamsBuilder.build());
        if (CollectionUtils.isEmpty(promotion.getData())) {
            return price;
        }

        IntSummaryStatistics tikiCoupon = promotion.getData().stream()
                .map(data -> getDiscountPrice("TIKI_COUPON").apply(price, data))
                .collect(Collectors.summarizingInt(Integer::intValue));
        IntSummaryStatistics sellerCoupon = promotion.getData().stream()
                .map(data -> getDiscountPrice("SELLER_COUPON").apply(price, data))
                .collect(Collectors.summarizingInt(Integer::intValue));
        return price - tikiCoupon.getMax() - sellerCoupon.getMax();
    }
}
