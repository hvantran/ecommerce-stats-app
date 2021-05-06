package com.hoatv.providers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Getter
@Setter
public class Product {

    private int id;
    private String sku;
    private String name;
    private String url_key;
    private String url_path;
    private String type;
    private Object book_cover;
    private String short_description;
    private int price;
    private int list_price;
    private double price_usd;
    private List<Badge> badges;
    private int discount;
    private int discount_rate;
    private int rating_average;
    private int review_count;
    private int order_count;
    private int favourite_count;
    private String thumbnail_url;
    private boolean has_ebook;
    private String inventory_status;
    @JsonProperty("is_visible")
    private boolean isVisible;
    private String productset_group_name;
    @JsonProperty("is_fresh")
    private boolean isFresh;
    private Object seller;
    @JsonProperty("is_flower")
    private boolean isFlower;
    @JsonProperty("is_gift_card")
    private boolean isGiftCard;
    private Inventory inventory;
    private String url_attendant_input_form;
    private Object master_id;
    private StockItem stock_item;
    private String salable_type;
    private int data_version;
    private int day_ago_created;
    private Categories categories;
    private Object meta_title;
    private Object meta_description;
    private Object meta_keywords;
    private boolean liked;
    private List<Object> rating_summary;
    private String description;
    private Object return_policy;
    private Object warranty_policy;
    private List<CustomAttribute> custom_attributes;
    private Brand brand;
    private List<SellerSpecification> seller_specifications;
    private CurrentSeller current_seller;
    private List<OtherSeller> other_sellers;
    private List<ConfigurableOption> configurable_options;
    private List<ConfigurableProduct> configurable_products;
    private List<Specification> specifications;
    private List<Object> product_links;
    private List<Object> services_and_promotions;
    private List<Object> promotions;
    private List<Image> images;
    private List<Rank> ranks;
    private List<Breadcrumb> breadcrumbs;
    private List<String> top_features;
    private Object installment_info;
    private String video_url;
    private String youtube;
    @JsonProperty("is_seller_in_chat_whitelist")
    private boolean isSellerInChatWhitelist;
    private String tala_request_id;
    private Error error;
    private String add_on_title;
    @JsonProperty("add_on")
    private List<AddOn> addOns;
    private PriceComparison price_comparison;
    private QuantitySold quantity_sold;
    private String inventory_type;

    @Getter
    public static class QuantitySold {
        private String text;
        private int value;
    }

    @Getter
    public static class PriceComparison{
        private String title;
        private String sub_title;
    }

    @Getter
    public static class Error {
        private String message;
    }

    @Getter
    public static class AddOn{
        private int id;
        private String name;
        private String thumbnail_url;
        private int price;
        private String add_on_description;
        private String add_on_information_title;
        private List<String> add_on_description_list;
        private int list_price;
        @JsonProperty("is_visible")
        private boolean isVisible;
        private String url;
        private String add_on_information_url;
    }


    @Getter
    public static class Badge{
        private String code;
        private int price;
        private int month;
        private String text;
    }

    @Getter
    public static class Inventory{
        private Object product_virtual_type;
        private String fulfillment_type;
    }

    @Getter
    public static class StockItem{
        private int qty;
        private int min_sale_qty;
        private int max_sale_qty;
        private boolean preorder_date;
    }

    @Getter
    public static class Categories{
        private int id;
        private String name;
        @JsonProperty("is_leaf")
        private boolean isLeaf;
    }

    @Getter
    public static class CustomAttribute{
        private String attribute;
        private String display_name;
        private String value;
    }

    @Getter
    public static class Brand{
        private int id;
        private String name;
        private String slug;
    }

    @Getter
    public static class SellerSpecification{
        private String name;
        private Object value;
        private Object url;
    }

    @Getter
    public static class CurrentSeller{
        private int id;
        private String sku;
        private int store_id;
        private String name;
        private String slug;
        private String link;
        @JsonProperty("is_best_store")
        private boolean isBestStore;
        private String logo;
        private String product_id;
        private int price;
        private Object is_offline_installment_supported;
    }

    @Getter
    public static class OtherSeller{
        private int id;
        private String name;
        private String link;
        private String logo;
        private String product_id;
        private int price;
        private int store_id;
    }

    @Getter
    public static class Attribute{
        private String name;
        private Object value;
    }

    @Getter
    public static class Specification{
        private String name;
        private List<Attribute> attributes;
    }

    @Getter
    public static class Image{
        private Object label;
        private Object position;
        private String base_url;
        private String thumbnail_url;
        private String small_url;
        private String medium_url;
        private String large_url;
        @JsonProperty("is_gallery")
        private boolean isGallery;
    }

    @Getter
    public static class Rank{
        private String type;
        private String period;
        private String rank;
        private String category_id;
        private String url;
        private String name;
    }

    @Getter
    public static class Breadcrumb{
        private String url;
        private String name;
        private int category_id;
    }

    @Getter
    public static class Value{
        private String label;
    }

    @Getter
    public static class ConfigurableOption{
        private String code;
        private String name;
        @JsonProperty("show_preview_image")
        private boolean isShowPreviewImage;
        private int position;
        private List<Value> values;
    }

    @Getter
    public static class Seller{
        private String name;
        private int id;
    }

    @Getter
    @FieldNameConstants
    public static class ConfigurableProduct{
        private int child_id;
        private String sku;
        private String name;
        private int price;
        private String thumbnail_url;
        private List<Image> images;
        private boolean selected;
        private String inventory_status;
        private int id;
        private int v5;
        private String option1;
        private String option2;
        private String option3;
        private String option4;
        private String option5;
        private String option6;
        private Seller seller;
    }
}
